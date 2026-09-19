package com.ruoyi.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.system.domain.MedStockFlow;
import com.ruoyi.system.domain.MedStockOrder;
import com.ruoyi.system.service.IMedStockOrderService;
import com.ruoyi.system.service.IMedStockService;

/** 入库、出库、退库及库存流水Controller */
@RestController
@RequestMapping("/system/stockOrder")
public class MedStockOrderController extends BaseController
{
    @Autowired
    private IMedStockOrderService orderService;

    @Autowired
    private IMedStockService stockService;

    @PreAuthorize("@ss.hasPermi('system:stockOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedStockOrder order)
    {
        startPage();
        return getDataTable(orderService.selectMedStockOrderList(order));
    }

    @PreAuthorize("@ss.hasPermi('system:stockOrder:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId)
    {
        return success(orderService.selectMedStockOrderById(orderId));
    }

    @PreAuthorize("@ss.hasPermi('system:stockOrder:add')")
    @Log(title = "库存业务单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedStockOrder order)
    {
        return toAjax(orderService.insertMedStockOrder(order));
    }

    @PreAuthorize("@ss.hasPermi('system:stockOrder:edit')")
    @Log(title = "库存业务单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedStockOrder order)
    {
        return toAjax(orderService.updateMedStockOrder(order));
    }

    @PreAuthorize("@ss.hasPermi('system:stockOrder:confirm')")
    @Log(title = "库存业务单确认", businessType = BusinessType.UPDATE)
    @PutMapping("/confirm/{orderId}")
    public AjaxResult confirm(@PathVariable Long orderId)
    {
        return toAjax(orderService.confirmMedStockOrder(orderId));
    }

    @PreAuthorize("@ss.hasPermi('system:stockOrder:remove')")
    @Log(title = "库存业务单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(orderService.deleteMedStockOrderByIds(orderIds));
    }

    @PreAuthorize("@ss.hasPermi('system:stockOrder:flow')")
    @GetMapping("/flow/list")
    public TableDataInfo flowList(MedStockFlow flow)
    {
        startPage();
        return getDataTable(stockService.selectMedStockFlowList(flow));
    }
}
