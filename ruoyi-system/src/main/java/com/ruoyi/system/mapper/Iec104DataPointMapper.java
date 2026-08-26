package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Iec104DataPoint;

/**
 * IEC104数据点 数据层
 * 
 * @author ruoyi
 */
public interface Iec104DataPointMapper
{
    /**
     * 查询数据点列表（按文件ID和IOA筛选）
     */
    public List<Iec104DataPoint> selectDataPointList(Iec104DataPoint dataPoint);

    /**
     * 批量新增数据点
     */
    public int batchInsertDataPoint(List<Iec104DataPoint> list);

    /**
     * 根据文件ID删除数据点
     */
    public int deleteDataPointByFileId(Long fileId);

    /**
     * 查询文件中的IOA列表（去重）
     */
    public List<Integer> selectDistinctIoaByFileId(Long fileId);

    /**
     * 查询图表数据（按文件ID和IOA列表查询）
     */
    public List<Iec104DataPoint> selectChartData(Long fileId, List<Integer> ioas);
}
