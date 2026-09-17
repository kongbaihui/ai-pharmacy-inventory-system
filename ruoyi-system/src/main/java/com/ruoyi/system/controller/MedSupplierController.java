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
import com.ruoyi.system.domain.MedSupplier;
import com.ruoyi.system.service.IMedSupplierService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 供应商信息Controller
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/system/supplier")
public class MedSupplierController extends BaseController
{
    @Autowired
    private IMedSupplierService medSupplierService;

    /**
     * 查询供应商信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:supplier:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedSupplier medSupplier)
    {
        startPage();
        List<MedSupplier> list = medSupplierService.selectMedSupplierList(medSupplier);
        return getDataTable(list);
    }

    /**
     * 查询供应商下拉选项（供药品信息选择使用）
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        MedSupplier medSupplier = new MedSupplier();
        medSupplier.setStatus("0");
        return success(medSupplierService.selectMedSupplierList(medSupplier));
    }

    /**
     * 导出供应商信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:supplier:export')")
    @Log(title = "供应商信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedSupplier medSupplier)
    {
        List<MedSupplier> list = medSupplierService.selectMedSupplierList(medSupplier);
        ExcelUtil<MedSupplier> util = new ExcelUtil<MedSupplier>(MedSupplier.class);
        util.exportExcel(response, list, "供应商信息数据");
    }

    /**
     * 获取供应商信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:supplier:query')")
    @GetMapping(value = "/{supplierId}")
    public AjaxResult getInfo(@PathVariable("supplierId") Long supplierId)
    {
        return success(medSupplierService.selectMedSupplierBySupplierId(supplierId));
    }

    /**
     * 新增供应商信息
     */
    @PreAuthorize("@ss.hasPermi('system:supplier:add')")
    @Log(title = "供应商信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedSupplier medSupplier)
    {
        return toAjax(medSupplierService.insertMedSupplier(medSupplier));
    }

    /**
     * 修改供应商信息
     */
    @PreAuthorize("@ss.hasPermi('system:supplier:edit')")
    @Log(title = "供应商信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedSupplier medSupplier)
    {
        return toAjax(medSupplierService.updateMedSupplier(medSupplier));
    }

    /**
     * 删除供应商信息
     */
    @PreAuthorize("@ss.hasPermi('system:supplier:remove')")
    @Log(title = "供应商信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{supplierIds}")
    public AjaxResult remove(@PathVariable Long[] supplierIds)
    {
        return toAjax(medSupplierService.deleteMedSupplierBySupplierIds(supplierIds));
    }
}
