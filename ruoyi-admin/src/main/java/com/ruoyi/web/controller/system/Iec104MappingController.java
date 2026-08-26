package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.Iec104IoaMapping;
import com.ruoyi.system.service.IIec104IoaMappingService;

/**
 * IEC104 IOA映射配置 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/iec104/mapping")
public class Iec104MappingController extends BaseController
{
    @Autowired
    private IIec104IoaMappingService ioaMappingService;

    /**
     * 查询映射配置列表
     */
    @PreAuthorize("@ss.hasPermi('iec104:mapping:list')")
    @GetMapping("/list")
    public TableDataInfo list(Iec104IoaMapping mapping)
    {
        startPage();
        List<Iec104IoaMapping> list = ioaMappingService.selectMappingList(mapping);
        return getDataTable(list);
    }

    /**
     * 获取映射配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('iec104:mapping:query')")
    @GetMapping("/{mappingId}")
    public AjaxResult getInfo(@PathVariable Long mappingId)
    {
        return success(ioaMappingService.selectMappingById(mappingId));
    }

    /**
     * 查询所有启用的映射配置（下拉选择用）
     */
    @GetMapping("/all")
    public AjaxResult listAll()
    {
        List<Iec104IoaMapping> list = ioaMappingService.selectAllEnabledMapping();
        return success(list);
    }

    /**
     * 新增映射配置
     */
    @PreAuthorize("@ss.hasPermi('iec104:mapping:add')")
    @Log(title = "IEC104点位映射", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Iec104IoaMapping mapping)
    {
        if (!ioaMappingService.checkIoaUnique(mapping))
        {
            return error("新增IOA'" + mapping.getIoa() + "'失败，该IOA地址已存在");
        }
        mapping.setCreateBy(getUsername());
        return toAjax(ioaMappingService.insertMapping(mapping));
    }

    /**
     * 修改映射配置
     */
    @PreAuthorize("@ss.hasPermi('iec104:mapping:edit')")
    @Log(title = "IEC104点位映射", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Iec104IoaMapping mapping)
    {
        if (!ioaMappingService.checkIoaUnique(mapping))
        {
            return error("修改IOA'" + mapping.getIoa() + "'失败，该IOA地址已存在");
        }
        mapping.setUpdateBy(getUsername());
        return toAjax(ioaMappingService.updateMapping(mapping));
    }

    /**
     * 删除映射配置
     */
    @PreAuthorize("@ss.hasPermi('iec104:mapping:remove')")
    @Log(title = "IEC104点位映射", businessType = BusinessType.DELETE)
    @DeleteMapping("/{mappingIds}")
    public AjaxResult remove(@PathVariable Long[] mappingIds)
    {
        return toAjax(ioaMappingService.deleteMappingByIds(mappingIds));
    }
}
