package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.Iec104IoaMapping;
import com.ruoyi.system.mapper.Iec104IoaMappingMapper;
import com.ruoyi.system.service.IIec104IoaMappingService;

/**
 * IEC104 IOA映射配置 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class Iec104IoaMappingServiceImpl implements IIec104IoaMappingService
{
    @Autowired
    private Iec104IoaMappingMapper ioaMappingMapper;

    @Override
    public List<Iec104IoaMapping> selectMappingList(Iec104IoaMapping mapping)
    {
        return ioaMappingMapper.selectMappingList(mapping);
    }

    @Override
    public Iec104IoaMapping selectMappingById(Long mappingId)
    {
        return ioaMappingMapper.selectMappingById(mappingId);
    }

    @Override
    public List<Iec104IoaMapping> selectAllEnabledMapping()
    {
        return ioaMappingMapper.selectAllEnabledMapping();
    }

    @Override
    public int insertMapping(Iec104IoaMapping mapping)
    {
        return ioaMappingMapper.insertMapping(mapping);
    }

    @Override
    public int updateMapping(Iec104IoaMapping mapping)
    {
        return ioaMappingMapper.updateMapping(mapping);
    }

    @Override
    public int deleteMappingByIds(Long[] mappingIds)
    {
        return ioaMappingMapper.deleteMappingByIds(mappingIds);
    }

    @Override
    public boolean checkIoaUnique(Iec104IoaMapping mapping)
    {
        Long mappingId = mapping.getMappingId() == null ? -1L : mapping.getMappingId();
        Iec104IoaMapping info = ioaMappingMapper.checkIoaUnique(mapping.getIoa());
        if (StringUtils.isNotNull(info) && !info.getMappingId().equals(mappingId))
        {
            return false; // IOA已存在
        }
        return true; // IOA唯一
    }
}
