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
import com.ruoyi.system.domain.MedStockCheck;
import com.ruoyi.system.domain.MedStockCheckItem;
import com.ruoyi.system.service.IMedStockCheckService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 库存盘点Controller
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/system/check")
public class MedStockCheckController extends BaseController
{
    @Autowired
    private IMedStockCheckService medStockCheckService;

    /**
     * 查询库存盘点列表
     */
    @PreAuthorize("@ss.hasPermi('system:check:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedStockCheck medStockCheck)
    {
        startPage();
        List<MedStockCheck> list = medStockCheckService.selectMedStockCheckList(medStockCheck);
        return getDataTable(list);
    }

    /**
     * 查询待盘点的账面明细（按当前批次库存生成）
     */
    @PreAuthorize("@ss.hasPermi('system:check:list')")
    @GetMapping("/bookItems")
    public AjaxResult bookItems(MedStockCheck medStockCheck)
    {
        return success(medStockCheckService.selectBookStockItemList(medStockCheck));
    }

    /**
     * 导出库存盘点列表
     */
    @PreAuthorize("@ss.hasPermi('system:check:export')")
    @Log(title = "库存盘点", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedStockCheck medStockCheck)
    {
        List<MedStockCheck> list = medStockCheckService.selectMedStockCheckList(medStockCheck);
        ExcelUtil<MedStockCheck> util = new ExcelUtil<MedStockCheck>(MedStockCheck.class);
        util.exportExcel(response, list, "库存盘点数据");
    }

    /**
     * 获取库存盘点详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:check:query')")
    @GetMapping(value = "/{checkId}")
    public AjaxResult getInfo(@PathVariable("checkId") Long checkId)
    {
        return success(medStockCheckService.selectMedStockCheckByCheckId(checkId));
    }

    /**
     * 新增库存盘点
     */
    @PreAuthorize("@ss.hasPermi('system:check:add')")
    @Log(title = "库存盘点", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedStockCheck medStockCheck)
    {
        return toAjax(medStockCheckService.insertMedStockCheck(medStockCheck));
    }

    /**
     * 修改库存盘点
     */
    @PreAuthorize("@ss.hasPermi('system:check:edit')")
    @Log(title = "库存盘点", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedStockCheck medStockCheck)
    {
        return toAjax(medStockCheckService.updateMedStockCheck(medStockCheck));
    }

    /**
     * 盘点审核（按盈亏数量调整库存）
     */
    @PreAuthorize("@ss.hasPermi('system:check:audit')")
    @Log(title = "库存盘点", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{checkId}")
    public AjaxResult audit(@PathVariable("checkId") Long checkId)
    {
        int adjustCount = medStockCheckService.auditMedStockCheck(checkId);
        return success("审核成功，共调整 " + adjustCount + " 条盈亏记录");
    }

    /**
     * 删除库存盘点
     */
    @PreAuthorize("@ss.hasPermi('system:check:remove')")
    @Log(title = "库存盘点", businessType = BusinessType.DELETE)
	@DeleteMapping("/{checkIds}")
    public AjaxResult remove(@PathVariable Long[] checkIds)
    {
        return toAjax(medStockCheckService.deleteMedStockCheckByCheckIds(checkIds));
    }
}
