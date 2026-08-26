package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.Iec104DataPoint;
import com.ruoyi.system.domain.Iec104LogFile;
import com.ruoyi.system.service.IIec104LogFileService;

/**
 * IEC104日志管理 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/iec104/log")
public class Iec104LogController extends BaseController
{
    @Autowired
    private IIec104LogFileService logFileService;

    /**
     * 查询日志文件列表
     */
    @PreAuthorize("@ss.hasPermi('iec104:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(Iec104LogFile logFile)
    {
        startPage();
        List<Iec104LogFile> list = logFileService.selectLogFileList(logFile);
        return getDataTable(list);
    }

    /**
     * 获取日志文件详细信息
     */
    @PreAuthorize("@ss.hasPermi('iec104:log:query')")
    @GetMapping("/{fileId}")
    public AjaxResult getInfo(@PathVariable Long fileId)
    {
        return success(logFileService.selectLogFileById(fileId));
    }

    /**
     * 上传并解析IEC104日志文件
     */
    @PreAuthorize("@ss.hasPermi('iec104:log:upload')")
    @Log(title = "IEC104日志管理", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "ioaBaseOffset", required = false, defaultValue = "0") Integer ioaBaseOffset) throws Exception
    {
        if (file.isEmpty())
        {
            return error("上传文件为空");
        }
        Iec104LogFile logFile = logFileService.uploadAndParse(file, getUsername(), ioaBaseOffset != null ? ioaBaseOffset : 0);
        return AjaxResult.success("解析成功", logFile);
    }

    /**
     * 删除日志文件
     */
    @PreAuthorize("@ss.hasPermi('iec104:log:remove')")
    @Log(title = "IEC104日志管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fileIds}")
    public AjaxResult remove(@PathVariable Long[] fileIds)
    {
        return toAjax(logFileService.deleteLogFileByIds(fileIds));
    }



    /**
     * 获取文件中的IOA列表（用于图表筛选）
     */
    @PreAuthorize("@ss.hasPermi('iec104:log:detail')")
    @GetMapping("/ioas/{fileId}")
    public AjaxResult getIoaList(@PathVariable Long fileId)
    {
        List<Integer> ioas = logFileService.selectDistinctIoaByFileId(fileId);
        return success(ioas);
    }

    /**
     * 获取图表数据
     */
    @PreAuthorize("@ss.hasPermi('iec104:log:detail')")
    @GetMapping("/chart/{fileId}")
    public AjaxResult getChartData(@PathVariable Long fileId,
            @RequestParam(value = "ioas", required = false) List<Integer> ioas)
    {
        return success(logFileService.selectChartData(fileId, ioas));
    }

    /**
     * 查询数据点列表（详情）
     */
    @PreAuthorize("@ss.hasPermi('iec104:log:detail')")
    @GetMapping("/data")
    public TableDataInfo dataPointList(Iec104DataPoint dataPoint)
    {
        startPage();
        List<Iec104DataPoint> list = logFileService.selectDataPointList(dataPoint);
        return getDataTable(list);
    }
}
