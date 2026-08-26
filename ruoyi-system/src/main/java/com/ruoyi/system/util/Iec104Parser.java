package com.ruoyi.system.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.system.domain.Iec104DataPoint;
import com.ruoyi.system.domain.Iec104IoaMapping;

/**
 * IEC104规约日志解析器
 * <p>
 * 解析格式：S/R + HH:MM:SS + 空格 + 十六进制字节序列
 * S帧(发送)包含 ASDU，Type ID=0x0D (M_ME_NC_1) 短浮点测量值
 * 每个信息对象 = 3字节IOA(小端序) + 4字节IEEE754浮点值(小端序)
 * </p>
 * <p>
 * 帧结构：
 * bytes[0] = 0x68 (起始字节)
 * bytes[1] = APDU长度(L)
 * bytes[2..5] = 控制域(4字节)
 * bytes[6] = Type ID (0x0D = M_ME_NC_1)
 * bytes[7] = VSQ (低7位=对象数量)
 * bytes[8..9] = 传送原因COT(2字节)
 * bytes[10..11] = CAS 公共地址(2字节LE)
 * bytes[12..] = 信息对象 (IOA 3字节LE + 浮点值 4字节LE + QDS品质1字节)，每对象8字节
 * </p>
 * <p>
 * IOA基地址偏移自动检测：第一遍扫描取最小原始IOA，自动扣除偏移，
 * 使解析结果与 iec104_ioa_mapping 配置表一致。
 * </p>
 * 
 * @author ruoyi
 */
public class Iec104Parser
{
    private static final Logger log = LoggerFactory.getLogger(Iec104Parser.class);

    /** Type ID: Measured, short floating point (M_ME_NC_1) */
    private static final int TYPE_ID_M_ME_NC_1 = 0x0D;

    /** 信息对象起始偏移: 6(APDU头) + 1(typeId) + 1(VSQ) + 2(COT) + 2(CAS) = 12, 对象从bytes[12]开始 */
    private static final int OBJECT_OFFSET = 12;

    /** 每个信息对象字节数: 3(IOA) + 1(QDS品质描述词) + 4(float value) = 8 */
    private static final int OBJECT_SIZE = 8;

    /**
     * 解析IEC104日志文件（自动检测IOA基地址偏移）
     * 
     * @param inputStream  文件输入流
     * @param mappingMap   IOA映射配置（key=IOA地址）
     * @return 解析结果
     */
    public static ParseResult parse(InputStream inputStream, Map<Integer, Iec104IoaMapping> mappingMap) throws Exception
    {
        return parse(inputStream, mappingMap, 0);
    }

    /**
     * 解析IEC104日志文件
     * 
     * @param inputStream   文件输入流
     * @param mappingMap    IOA映射配置（key=IOA地址）
     * @param ioaBaseOffset IOA基地址偏移（0=自动检测，>0=手动指定）
     * @return 解析结果
     */
    public static ParseResult parse(InputStream inputStream, Map<Integer, Iec104IoaMapping> mappingMap, int ioaBaseOffset) throws Exception
    {
        // 1. 读取所有行
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (!line.isEmpty())
                {
                    lines.add(line);
                }
            }
        }

        // 2. 第一遍扫描：提取所有原始IOA，找最小值
        int minIoa = Integer.MAX_VALUE;
        int totalFrames = lines.size();

        if (ioaBaseOffset == 0)
        {
            for (String line : lines)
            {
                List<Integer> rawIoas = extractRawIoas(line);
                for (Integer rawIoa : rawIoas)
                {
                    if (rawIoa > 0 && rawIoa < minIoa)
                    {
                        minIoa = rawIoa;
                    }
                }
            }

            // 自动检测基地址偏移
            // 最小原始IOA对应真实IOA=1，因此 offset = minIoa - 1
            // 例如: minIoa=0x4001 -> offset=0x4000, 使 IOA 1,3,5... 正确还原
            if (minIoa == Integer.MAX_VALUE || minIoa <= 1)
            {
                ioaBaseOffset = 0;
            }
            else
            {
                ioaBaseOffset = minIoa - 1;
            }
            log.info("自动检测IOA基地址偏移: {} (0x{}), 最小原始IOA: {} (0x{})", 
                    ioaBaseOffset, Integer.toHexString(ioaBaseOffset), 
                    minIoa, Integer.toHexString(minIoa));
        }

        // 3. 第二遍扫描：正式解析数据点
        List<Iec104DataPoint> dataPoints = new ArrayList<>();
        int frameSeq = 0;

        for (String line : lines)
        {
            try
            {
                if (line.length() < 10)
                {
                    continue;
                }

                // 解析帧类型: S=发送帧(含数据), R=接收确认帧(无数据)
                String frameType = line.substring(0, 1);
                // 解析帧时间: HH:MM:SS
                String frameTime = line.substring(1, 9);

                // R帧为确认帧，不包含测量数据，跳过
                if (!"S".equals(frameType))
                {
                    continue;
                }

                // 解析十六进制字节
                String hexPart = line.substring(10).trim();
                byte[] bytes = hexStringToByteArray(hexPart);

                if (bytes.length < OBJECT_OFFSET + OBJECT_SIZE)
                {
                    continue;
                }

                int typeId = bytes[6] & 0xFF;
                // 只处理 M_ME_NC_1 (短浮点测量值)
                if (typeId != TYPE_ID_M_ME_NC_1)
                {
                    continue;
                }

                int vsq = bytes[7] & 0xFF;
                int objectCount = vsq & 0x7F; // 低7位为对象数量

                int offset = OBJECT_OFFSET;

                for (int i = 0; i < objectCount && offset + OBJECT_SIZE <= bytes.length; i++)
                {
                    frameSeq++;

                    // IOA: 3 bytes little-endian
                    int rawIoa = (bytes[offset] & 0xFF)
                            | ((bytes[offset + 1] & 0xFF) << 8)
                            | ((bytes[offset + 2] & 0xFF) << 16);
                    // 扣除基地址偏移，得到真实IOA
                    int ioa = rawIoa - ioaBaseOffset;

                    // Value: 4 bytes IEEE 754 float little-endian (紧接IOA之后)
                    float value = ByteBuffer.wrap(bytes, offset + 3, 4)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .getFloat();

                    // 过滤无效值(NaN/Infinity)
                    if (Float.isNaN(value) || Float.isInfinite(value))
                    {
                        log.debug("跳过无效值: IOA={}, value={}", ioa, value);
                        offset += OBJECT_SIZE;
                        continue;
                    }

                    // bytes[offset+7] = QDS 品质描述词 (跳过)

                    Iec104DataPoint point = new Iec104DataPoint();
                    point.setFrameType(frameType);
                    point.setFrameTime(frameTime);
                    point.setFrameSeq(frameSeq);
                    point.setIoa(ioa);
                    point.setRawValue((double) value);

                    // 根据映射配置设置量名称和单位
                    Iec104IoaMapping mapping = mappingMap.get(ioa);
                    if (mapping != null)
                    {
                        point.setQuantityName(mapping.getQuantityName());
                        point.setQuantityUnit(mapping.getUnit());
                    }
                    else
                    {
                        point.setQuantityName("IOA_" + ioa);
                        point.setQuantityUnit("");
                    }

                    dataPoints.add(point);
                    
                    // 记录前几个数据点的解析详情，方便调试
                    if (frameSeq <= 10)
                    {
                        log.info("数据点{}: IOA={} (raw=0x{}), value={}, name={}", 
                                frameSeq, ioa, Integer.toHexString(rawIoa),
                                value, point.getQuantityName());
                    }
                    
                    offset += OBJECT_SIZE;
                }
            }
            catch (Exception e)
            {
                log.warn("解析行失败: {}, 错误: {}", line, e.getMessage());
            }
        }

        ParseResult result = new ParseResult();
        result.setDataPoints(dataPoints);
        result.setTotalFrames(totalFrames);
        result.setTotalPoints(dataPoints.size());
        result.setIoaBaseOffset(ioaBaseOffset);
        return result;
    }

    /**
     * 从一行日志中提取所有原始IOA值（用于自动检测基地址偏移）
     */
    private static List<Integer> extractRawIoas(String line)
    {
        List<Integer> ioas = new ArrayList<>();
        try
        {
            if (line.length() < 10 || !line.startsWith("S"))
            {
                return ioas;
            }

            String hexPart = line.substring(10).trim();
            byte[] bytes = hexStringToByteArray(hexPart);

            if (bytes.length < OBJECT_OFFSET + OBJECT_SIZE)
            {
                return ioas;
            }

            int typeId = bytes[6] & 0xFF;
            if (typeId != TYPE_ID_M_ME_NC_1)
            {
                return ioas;
            }

            int vsq = bytes[7] & 0xFF;
            int objectCount = vsq & 0x7F;
            int offset = OBJECT_OFFSET;

            for (int i = 0; i < objectCount && offset + OBJECT_SIZE <= bytes.length; i++)
            {
                int rawIoa = (bytes[offset] & 0xFF)
                        | ((bytes[offset + 1] & 0xFF) << 8)
                        | ((bytes[offset + 2] & 0xFF) << 16);
                if (rawIoa > 0)
                {
                    ioas.add(rawIoa);
                }
                offset += OBJECT_SIZE;
            }
        }
        catch (Exception e)
        {
            // 忽略解析错误
        }
        return ioas;
    }

    /**
     * 十六进制字符串转字节数组
     */
    private static byte[] hexStringToByteArray(String hex)
    {
        String[] parts = hex.split("\\s+");
        byte[] bytes = new byte[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            bytes[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return bytes;
    }

    /**
     * 构建IOA映射Map
     */
    public static Map<Integer, Iec104IoaMapping> buildMappingMap(List<Iec104IoaMapping> mappings)
    {
        Map<Integer, Iec104IoaMapping> map = new HashMap<>();
        if (mappings != null)
        {
            for (Iec104IoaMapping mapping : mappings)
            {
                map.put(mapping.getIoa(), mapping);
            }
        }
        return map;
    }

    /**
     * 解析结果
     */
    public static class ParseResult
    {
        private List<Iec104DataPoint> dataPoints;
        private int totalFrames;
        private int totalPoints;
        private int ioaBaseOffset;

        public List<Iec104DataPoint> getDataPoints()
        {
            return dataPoints;
        }

        public void setDataPoints(List<Iec104DataPoint> dataPoints)
        {
            this.dataPoints = dataPoints;
        }

        public int getTotalFrames()
        {
            return totalFrames;
        }

        public void setTotalFrames(int totalFrames)
        {
            this.totalFrames = totalFrames;
        }

        public int getTotalPoints()
        {
            return totalPoints;
        }

        public void setTotalPoints(int totalPoints)
        {
            this.totalPoints = totalPoints;
        }

        public int getIoaBaseOffset()
        {
            return ioaBaseOffset;
        }

        public void setIoaBaseOffset(int ioaBaseOffset)
        {
            this.ioaBaseOffset = ioaBaseOffset;
        }
    }
}
