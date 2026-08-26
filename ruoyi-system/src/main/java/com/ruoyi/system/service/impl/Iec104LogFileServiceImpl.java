package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.system.domain.Iec104DataPoint;
import com.ruoyi.system.domain.Iec104IoaMapping;
import com.ruoyi.system.domain.Iec104LogFile;
import com.ruoyi.system.mapper.Iec104DataPointMapper;
import com.ruoyi.system.mapper.Iec104IoaMappingMapper;
import com.ruoyi.system.mapper.Iec104LogFileMapper;
import com.ruoyi.system.service.IIec104LogFileService;
import com.ruoyi.system.util.Iec104Parser;

/**
 * IEC104日志文件 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class Iec104LogFileServiceImpl implements IIec104LogFileService
{
    private static final Logger log = LoggerFactory.getLogger(Iec104LogFileServiceImpl.class);

    /** 批量插入每批大小 */
    private static final int BATCH_SIZE = 500;

    @Autowired
    private Iec104LogFileMapper logFileMapper;

    @Autowired
    private Iec104DataPointMapper dataPointMapper;

    @Autowired
    private Iec104IoaMappingMapper ioaMappingMapper;

    @Override
    public List<Iec104LogFile> selectLogFileList(Iec104LogFile logFile)
    {
        return logFileMapper.selectLogFileList(logFile);
    }

    @Override
    public Iec104LogFile selectLogFileById(Long fileId)
    {
        return logFileMapper.selectLogFileById(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Iec104LogFile uploadAndParse(MultipartFile file, String username, int ioaBaseOffset) throws Exception
    {
        // 1. 保存日志文件记录
        Iec104LogFile logFile = new Iec104LogFile();
        logFile.setFileName(file.getOriginalFilename());
        logFile.setFilePath("");
        logFile.setFileSize(file.getSize());
        logFile.setCreateBy(username);
        logFileMapper.insertLogFile(logFile);

        try
        {
            // 2. 获取所有启用的IOA映射配置
            List<Iec104IoaMapping> mappings = ioaMappingMapper.selectAllEnabledMapping();
            Map<Integer, Iec104IoaMapping> mappingMap = Iec104Parser.buildMappingMap(mappings);

            // 3. 解析日志文件
            Iec104Parser.ParseResult parseResult = Iec104Parser.parse(file.getInputStream(), mappingMap, ioaBaseOffset);

            // 4. 更新文件统计信息
            logFile.setTotalFrames(parseResult.getTotalFrames());
            logFile.setTotalPoints(parseResult.getTotalPoints());
            logFile.setStatus("0"); // 成功
            logFile.setIoaBaseOffset(parseResult.getIoaBaseOffset());

            // 5. 批量插入数据点
            List<Iec104DataPoint> dataPoints = parseResult.getDataPoints();
            for (Iec104DataPoint point : dataPoints)
            {
                point.setFileId(logFile.getFileId());
            }

            // 分批插入
            for (int i = 0; i < dataPoints.size(); i += BATCH_SIZE)
            {
                int end = Math.min(i + BATCH_SIZE, dataPoints.size());
                List<Iec104DataPoint> batch = dataPoints.subList(i, end);
                dataPointMapper.batchInsertDataPoint(batch);
            }

            logFileMapper.updateLogFile(logFile);
            log.info("IEC104日志文件解析成功: {}, 总帧数: {}, 数据点: {}, IOA基地址偏移: {}(0x{})", 
                    file.getOriginalFilename(), parseResult.getTotalFrames(), parseResult.getTotalPoints(),
                    parseResult.getIoaBaseOffset(), Integer.toHexString(parseResult.getIoaBaseOffset()));
        }
        catch (Exception e)
        {
            log.error("IEC104日志文件解析失败: {}", file.getOriginalFilename(), e);
            logFile.setStatus("1"); // 失败
            logFile.setErrorMsg(e.getMessage());
            logFileMapper.updateLogFile(logFile);
            throw e;
        }

        return logFile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLogFileByIds(Long[] fileIds)
    {
        // 先删除关联的数据点
        for (Long fileId : fileIds)
        {
            dataPointMapper.deleteDataPointByFileId(fileId);
        }
        return logFileMapper.deleteLogFileByIds(fileIds);
    }

    @Override
    public Map<String, Object> selectChartData(Long fileId, List<Integer> ioas)
    {
        // 查询数据点
        List<Iec104DataPoint> points = dataPointMapper.selectChartData(fileId, ioas);

        // 按IOA分组，构建图表数据
        // key: ioa (带量名称), value: { times: [], values: [] }
        Map<Integer, Map<String, Object>> seriesMap = new LinkedHashMap<>();

        for (Iec104DataPoint point : points)
        {
            Integer ioa = point.getIoa();
            if (!seriesMap.containsKey(ioa))
            {
                Map<String, Object> series = new HashMap<>();
                String name = point.getQuantityName();
                if (point.getQuantityUnit() != null && !point.getQuantityUnit().isEmpty())
                {
                    name = name + "(" + point.getQuantityUnit() + ")";
                }
                series.put("name", name);
                series.put("ioa", ioa);
                series.put("unit", point.getQuantityUnit());
                series.put("times", new ArrayList<String>());
                series.put("values", new ArrayList<Double>());
                seriesMap.put(ioa, series);
            }

            Map<String, Object> series = seriesMap.get(ioa);
            @SuppressWarnings("unchecked")
            List<String> times = (List<String>) series.get("times");
            @SuppressWarnings("unchecked")
            List<Double> values = (List<Double>) series.get("values");

            // X轴使用 帧序号 + 帧时间 作为标签
            times.add(point.getFrameSeq() + "-" + point.getFrameTime());
            values.add(point.getRawValue());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("series", new ArrayList<>(seriesMap.values()));
        // 提取所有时间标签（取第一个IOA的时间轴作为公共X轴）
        if (!seriesMap.isEmpty())
        {
            Map<String, Object> firstSeries = seriesMap.values().iterator().next();
            result.put("xAxis", firstSeries.get("times"));
        }
        else
        {
            result.put("xAxis", new ArrayList<>());
        }
        return result;
    }

    @Override
    public List<Integer> selectDistinctIoaByFileId(Long fileId)
    {
        return dataPointMapper.selectDistinctIoaByFileId(fileId);
    }

    @Override
    public List<Iec104DataPoint> selectDataPointList(Iec104DataPoint dataPoint)
    {
        return dataPointMapper.selectDataPointList(dataPoint);
    }
}
