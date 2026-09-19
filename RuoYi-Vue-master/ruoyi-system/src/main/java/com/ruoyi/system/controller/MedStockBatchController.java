package com.ruoyi.system.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.MedStockBatch;
import com.ruoyi.system.service.IMedStockBatchService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 药品有效期（批次）Controller
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/system/batch")
public class MedStockBatchController extends BaseController
{
    @Autowired
    private IMedStockBatchService medStockBatchService;

    /**
     * 查询药品库存批次列表（支持按临期、过期、批号、药品筛选）
     */
    @PreAuthorize("@ss.hasPermi('system:batch:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedStockBatch medStockBatch)
    {
        startPage();
        List<MedStockBatch> list = medStockBatchService.selectMedStockBatchList(medStockBatch);
        return getDataTable(list);
    }

    /**
     * 查询临期与过期药品统计（供库存预警与数据看板使用）
     */
    @PreAuthorize("@ss.hasPermi('system:batch:list')")
    @GetMapping("/expirySummary")
    public AjaxResult expirySummary()
    {
        return success(medStockBatchService.selectExpirySummary());
    }

    /**
     * 导出药品有效期列表
     */
    @PreAuthorize("@ss.hasPermi('system:batch:export')")
    @Log(title = "药品有效期", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedStockBatch medStockBatch)
    {
        List<MedStockBatch> list = medStockBatchService.selectMedStockBatchList(medStockBatch);
        ExcelUtil<MedStockBatch> util = new ExcelUtil<MedStockBatch>(MedStockBatch.class);
        util.exportExcel(response, list, "药品有效期数据");
    }

    /**
     * 获取药品库存批次详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:batch:query')")
    @GetMapping(value = "/{batchId}")
    public AjaxResult getInfo(@PathVariable("batchId") Long batchId)
    {
        return success(medStockBatchService.selectMedStockBatchByBatchId(batchId));
    }

    /**
     * 维护批次效期信息
     * 
     * 批次数量由入库、出库、盘点、过期清理等业务变更，本接口只允许修改
     * 生产日期、有效期、供应商与备注，避免库存总量与批次数量不一致。
     */
    @PreAuthorize("@ss.hasPermi('system:batch:edit')")
    @Log(title = "药品有效期", businessType = BusinessType.UPDATE)
    @PutMapping("/expiry")
    public AjaxResult expiry(@RequestBody MedStockBatch medStockBatch)
    {
        return toAjax(medStockBatchService.updateBatchExpiry(medStockBatch));
    }

    /**
     * 刷新批次效期状态（按药品近效期预警天数重新判定正常、临期、过期）
     */
    @PreAuthorize("@ss.hasPermi('system:batch:edit')")
    @Log(title = "药品有效期", businessType = BusinessType.UPDATE)
    @PutMapping("/refresh")
    public AjaxResult refresh()
    {
        medStockBatchService.refreshBatchStatus();
        return success("效期状态刷新完成");
    }

    /**
     * 按批次剩余数量重算库存总量
     * 
     * 用于修正批次剩余数量之和与库存总量不一致的数据，
     * 调整过程统一走库存变更服务，会同步写入库存流水。
     */
    @PreAuthorize("@ss.hasPermi('system:batch:recalc')")
    @Log(title = "药品有效期", businessType = BusinessType.UPDATE)
    @PostMapping("/recalcStock")
    public AjaxResult recalcStock()
    {
        return success(medStockBatchService.recalcStockQty());
    }
}
