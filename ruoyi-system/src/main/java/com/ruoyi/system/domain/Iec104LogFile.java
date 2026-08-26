package com.ruoyi.system.domain;

import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * IEC104日志文件对象 iec104_log_file
 * 
 * @author ruoyi
 */
public class Iec104LogFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    @Excel(name = "文件ID", cellType = ColumnType.NUMERIC)
    private Long fileId;

    /** 文件名称 */
    @Excel(name = "文件名称")
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件大小(字节) */
    @Excel(name = "文件大小(字节)", cellType = ColumnType.NUMERIC)
    private Long fileSize;

    /** 总帧数 */
    @Excel(name = "总帧数", cellType = ColumnType.NUMERIC)
    private Integer totalFrames;

    /** 总数据点数 */
    @Excel(name = "总数据点数", cellType = ColumnType.NUMERIC)
    private Integer totalPoints;

    /** 解析状态（0成功 1失败） */
    @Excel(name = "解析状态", readConverterExp = "0=成功,1=失败")
    private String status;

    /** 错误信息 */
    private String errorMsg;

    /** 自动检测的IOA基地址偏移（非数据库字段，仅用于返回给前端） */
    private Integer ioaBaseOffset;

    /** 数据点列表（非数据库字段） */
    private List<Iec104DataPoint> dataPoints;

    public Long getFileId()
    {
        return fileId;
    }

    public void setFileId(Long fileId)
    {
        this.fileId = fileId;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public Integer getTotalFrames()
    {
        return totalFrames;
    }

    public void setTotalFrames(Integer totalFrames)
    {
        this.totalFrames = totalFrames;
    }

    public Integer getTotalPoints()
    {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints)
    {
        this.totalPoints = totalPoints;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public Integer getIoaBaseOffset()
    {
        return ioaBaseOffset;
    }

    public void setIoaBaseOffset(Integer ioaBaseOffset)
    {
        this.ioaBaseOffset = ioaBaseOffset;
    }

    public List<Iec104DataPoint> getDataPoints()
    {
        return dataPoints;
    }

    public void setDataPoints(List<Iec104DataPoint> dataPoints)
    {
        this.dataPoints = dataPoints;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("fileId", getFileId())
            .append("fileName", getFileName())
            .append("filePath", getFilePath())
            .append("fileSize", getFileSize())
            .append("totalFrames", getTotalFrames())
            .append("totalPoints", getTotalPoints())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
