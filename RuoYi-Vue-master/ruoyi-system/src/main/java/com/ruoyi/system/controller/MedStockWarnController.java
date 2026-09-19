package com.ruoyi.system.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.MedStockWarn;
import com.ruoyi.system.service.IMedStockWarnService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 库存预警Controller
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/system/warn")
public class MedStockWarnController extends BaseController
{
    @Autowired
    private IMedStockWarnService medStockWarnService;

    /**
     * 查询库存预警列表
     */
    @PreAuthorize("@ss.hasPermi('system:warn:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedStockWarn medStockWarn)
    {
        startPage();
        List<MedStockWarn> list = medStockWarnService.selectMedStockWarnList(medStockWarn);
        return getDataTable(list);
    }

    /**
     * 查询库存预警统计（供库存预警页面与数据看板使用）
     */
    @PreAuthorize("@ss.hasPermi('system:warn:list')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(medStockWarnService.selectWarnSummary());
    }

    /**
     * 导出库存预警列表
     */
    @PreAuthorize("@ss.hasPermi('system:warn:export')")
    @Log(title = "库存预警", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedStockWarn medStockWarn)
    {
        List<MedStockWarn> list = medStockWarnService.selectMedStockWarnList(medStockWarn);
        ExcelUtil<MedStockWarn> util = new ExcelUtil<MedStockWarn>(MedStockWarn.class);
        util.exportExcel(response, list, "库存预警数据");
    }

    /**
     * 获取库存预警详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:warn:query')")
    @GetMapping(value = "/{warnId}")
    public AjaxResult getInfo(@PathVariable("warnId") Long warnId)
    {
        return success(medStockWarnService.selectMedStockWarnByWarnId(warnId));
    }

    /**
     * 扫描生成库存预警（库存不足、库存积压、近效期、已过期）
     */
    @PreAuthorize("@ss.hasPermi('system:warn:scan')")
    @Log(title = "库存预警", businessType = BusinessType.INSERT)
    @PostMapping("/scan")
    public AjaxResult scan()
    {
        int count = medStockWarnService.scanStockWarn();
        return success("预警扫描完成，本次新增 " + count + " 条预警记录");
    }

    /**
     * 新增库存预警
     */
    @PreAuthorize("@ss.hasPermi('system:warn:add')")
    @Log(title = "库存预警", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedStockWarn medStockWarn)
    {
        return toAjax(medStockWarnService.insertMedStockWarn(medStockWarn));
    }

    /**
     * 修改库存预警
     */
    @PreAuthorize("@ss.hasPermi('system:warn:edit')")
    @Log(title = "库存预警", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedStockWarn medStockWarn)
    {
        return toAjax(medStockWarnService.updateMedStockWarn(medStockWarn));
    }

    /**
     * 处理预警（支持批量处理，handleStatus 为 1 已处理、2 已忽略）
     */
    @PreAuthorize("@ss.hasPermi('system:warn:handle')")
    @Log(title = "库存预警", businessType = BusinessType.UPDATE)
    @PutMapping("/handle")
    public AjaxResult handle(@RequestBody MedStockWarn medStockWarn)
    {
        return toAjax(medStockWarnService.handleMedStockWarn(medStockWarn));
    }

    /**
     * 删除库存预警
     */
    @PreAuthorize("@ss.hasPermi('system:warn:remove')")
    @Log(title = "库存预警", businessType = BusinessType.DELETE)
	@DeleteMapping("/{warnIds}")
    public AjaxResult remove(@PathVariable Long[] warnIds)
    {
        return toAjax(medStockWarnService.deleteMedStockWarnByWarnIds(warnIds));
    }
}
