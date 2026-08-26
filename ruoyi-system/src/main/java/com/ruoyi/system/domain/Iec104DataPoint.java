package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * IEC104数据点对象 iec104_data_point
 * 
 * @author ruoyi
 */
public class Iec104DataPoint
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 文件ID */
    private Long fileId;

    /** 帧类型（S发送 R接收） */
    private String frameType;

    /** 帧时间（HH:MM:SS） */
    private String frameTime;

    /** 帧序号 */
    private Integer frameSeq;

    /** 信息对象地址IOA */
    private Integer ioa;

    /** 原始测量值（IEEE754浮点） */
    private Double rawValue;

    /** 量名称（来自映射配置） */
    private String quantityName;

    /** 量单位（来自映射配置） */
    private String quantityUnit;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getFileId()
    {
        return fileId;
    }

    public void setFileId(Long fileId)
    {
        this.fileId = fileId;
    }

    public String getFrameType()
    {
        return frameType;
    }

    public void setFrameType(String frameType)
    {
        this.frameType = frameType;
    }

    public String getFrameTime()
    {
        return frameTime;
    }

    public void setFrameTime(String frameTime)
    {
        this.frameTime = frameTime;
    }

    public Integer getFrameSeq()
    {
        return frameSeq;
    }

    public void setFrameSeq(Integer frameSeq)
    {
        this.frameSeq = frameSeq;
    }

    public Integer getIoa()
    {
        return ioa;
    }

    public void setIoa(Integer ioa)
    {
        this.ioa = ioa;
    }

    public Double getRawValue()
    {
        return rawValue;
    }

    public void setRawValue(Double rawValue)
    {
        this.rawValue = rawValue;
    }

    public String getQuantityName()
    {
        return quantityName;
    }

    public void setQuantityName(String quantityName)
    {
        this.quantityName = quantityName;
    }

    public String getQuantityUnit()
    {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit)
    {
        this.quantityUnit = quantityUnit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("fileId", getFileId())
            .append("frameType", getFrameType())
            .append("frameTime", getFrameTime())
            .append("frameSeq", getFrameSeq())
            .append("ioa", getIoa())
            .append("rawValue", getRawValue())
            .append("quantityName", getQuantityName())
            .append("quantityUnit", getQuantityUnit())
            .toString();
    }
}
