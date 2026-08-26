package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Iec104LogFile;

/**
 * IEC104日志文件 数据层
 * 
 * @author ruoyi
 */
public interface Iec104LogFileMapper
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
     * 新增日志文件
     */
    public int insertLogFile(Iec104LogFile logFile);

    /**
     * 修改日志文件
     */
    public int updateLogFile(Iec104LogFile logFile);

    /**
     * 删除日志文件
     */
    public int deleteLogFileById(Long fileId);

    /**
     * 批量删除日志文件
     */
    public int deleteLogFileByIds(Long[] fileIds);
}
