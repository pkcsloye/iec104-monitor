package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.system.domain.Iec104DataPoint;
import com.ruoyi.system.domain.Iec104LogFile;

/**
 * IEC104日志文件 服务层
 * 
 * @author ruoyi
 */
public interface IIec104LogFileService
{
    /**
     * 查询日志文件列表
     */
    public List<Iec104LogFile> selectLogFileList(Iec104LogFile logFile);

    /**
     * 通过ID查询日志文件
     */
    public Iec104LogFile selectLogFileById(Long fileId);

    /**
     * 上传并解析IEC104日志文件
     * 
     * @param file          日志文件
     * @param username      操作用户
     * @param ioaBaseOffset IOA基地址偏移（部分设备IOA从0x4000开始，设为16384可还原真实IOA，默认0）
     */
    public Iec104LogFile uploadAndParse(MultipartFile file, String username, int ioaBaseOffset) throws Exception;

    /**
     * 删除日志文件（同时删除关联数据点）
     */
    public int deleteLogFileByIds(Long[] fileIds);

    /**
     * 查询图表数据
     */
    public Map<String, Object> selectChartData(Long fileId, List<Integer> ioas);

    /**
     * 查询文件中的IOA列表
     */
    public List<Integer> selectDistinctIoaByFileId(Long fileId);

    /**
     * 查询数据点列表
     */
    public List<Iec104DataPoint> selectDataPointList(Iec104DataPoint dataPoint);
}
