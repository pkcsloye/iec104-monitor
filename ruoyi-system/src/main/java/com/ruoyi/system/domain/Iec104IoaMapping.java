package com.ruoyi.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * IEC104 IOA映射配置对象 iec104_ioa_mapping
 * 
 * @author ruoyi
 */
public class Iec104IoaMapping extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 映射ID */
    @Excel(name = "映射ID", cellType = ColumnType.NUMERIC)
    private Long mappingId;

    /** 信息对象地址IOA */
    @Excel(name = "IOA地址")
    @NotNull(message = "IOA地址不能为空")
    private Integer ioa;

    /** 量名称 */
    @Excel(name = "量名称")
    @NotBlank(message = "量名称不能为空")
    private String quantityName;

    /** 量类型 */
    @Excel(name = "量类型")
    private String quantityType;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 描述 */
    @Excel(name = "描述")
    private String description;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getMappingId()
    {
        return mappingId;
    }

    public void setMappingId(Long mappingId)
    {
        this.mappingId = mappingId;
    }

    public Integer getIoa()
    {
        return ioa;
    }

    public void setIoa(Integer ioa)
    {
        this.ioa = ioa;
    }

    public String getQuantityName()
    {
        return quantityName;
    }

    public void setQuantityName(String quantityName)
    {
        this.quantityName = quantityName;
    }

    public String getQuantityType()
    {
        return quantityType;
    }

    public void setQuantityType(String quantityType)
    {
        this.quantityType = quantityType;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("mappingId", getMappingId())
            .append("ioa", getIoa())
            .append("quantityName", getQuantityName())
            .append("quantityType", getQuantityType())
            .append("unit", getUnit())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
