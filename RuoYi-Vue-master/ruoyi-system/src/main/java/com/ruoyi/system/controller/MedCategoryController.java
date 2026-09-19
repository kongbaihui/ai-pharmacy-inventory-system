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
import com.ruoyi.system.domain.MedCategory;
import com.ruoyi.system.service.IMedCategoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 药品分类Controller
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/system/category")
public class MedCategoryController extends BaseController
{
    @Autowired
    private IMedCategoryService medCategoryService;

    /**
     * 查询药品分类列表（树形表格数据）
     */
    @PreAuthorize("@ss.hasPermi('system:category:list')")
    @GetMapping("/list")
    public AjaxResult list(MedCategory medCategory)
    {
        List<MedCategory> list = medCategoryService.selectMedCategoryList(medCategory);
        return success(list);
    }

    /**
     * 查询药品分类下拉树结构
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(MedCategory medCategory)
    {
        List<MedCategory> list = medCategoryService.selectMedCategoryList(medCategory);
        return success(list);
    }

    /**
     * 导出药品分类列表
     */
    @PreAuthorize("@ss.hasPermi('system:category:export')")
    @Log(title = "药品分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedCategory medCategory)
    {
        List<MedCategory> list = medCategoryService.selectMedCategoryList(medCategory);
        ExcelUtil<MedCategory> util = new ExcelUtil<MedCategory>(MedCategory.class);
        util.exportExcel(response, list, "药品分类数据");
    }

    /**
     * 获取药品分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(medCategoryService.selectMedCategoryByCategoryId(categoryId));
    }

    /**
     * 新增药品分类
     */
    @PreAuthorize("@ss.hasPermi('system:category:add')")
    @Log(title = "药品分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedCategory medCategory)
    {
        return toAjax(medCategoryService.insertMedCategory(medCategory));
    }

    /**
     * 修改药品分类
     */
    @PreAuthorize("@ss.hasPermi('system:category:edit')")
    @Log(title = "药品分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedCategory medCategory)
    {
        return toAjax(medCategoryService.updateMedCategory(medCategory));
    }

    /**
     * 删除药品分类
     */
    @PreAuthorize("@ss.hasPermi('system:category:remove')")
    @Log(title = "药品分类", businessType = BusinessType.DELETE)
	@DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(medCategoryService.deleteMedCategoryByCategoryIds(categoryIds));
    }
}
