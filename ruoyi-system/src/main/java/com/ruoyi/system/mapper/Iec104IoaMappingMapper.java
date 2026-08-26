package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Iec104IoaMapping;

/**
 * IEC104 IOA映射配置 数据层
 * 
 * @author ruoyi
 */
public interface Iec104IoaMappingMapper
{
    /**
     * 查询映射配置列表
     */
    public List<Iec104IoaMapping> selectMappingList(Iec104IoaMapping mapping);

    /**
     * 通过ID查询映射配置
     */
    public Iec104IoaMapping selectMappingById(Long mappingId);

    /**
     * 通过IOA查询映射配置
     */
    public Iec104IoaMapping selectMappingByIoa(Integer ioa);

    /**
     * 查询所有启用的映射配置
     */
    public List<Iec104IoaMapping> selectAllEnabledMapping();

    /**
     * 新增映射配置
     */
    public int insertMapping(Iec104IoaMapping mapping);

    /**
     * 修改映射配置
     */
    public int updateMapping(Iec104IoaMapping mapping);

    /**
     * 删除映射配置
     */
    public int deleteMappingById(Long mappingId);

    /**
     * 批量删除映射配置
     */
    public int deleteMappingByIds(Long[] mappingIds);

    /**
     * 检查IOA是否唯一
     */
    public Iec104IoaMapping checkIoaUnique(Integer ioa);
}
