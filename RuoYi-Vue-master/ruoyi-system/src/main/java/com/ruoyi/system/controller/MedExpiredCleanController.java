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
import com.ruoyi.system.domain.MedExpiredClean;
import com.ruoyi.system.service.IMedExpiredCleanService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 过期药品清理Controller
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/system/expiredclean")
public class MedExpiredCleanController extends BaseController
{
    @Autowired
    private IMedExpiredCleanService medExpiredCleanService;

    /**
     * 查询过期药品清理列表
     */
    @PreAuthorize("@ss.hasPermi('system:expiredclean:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedExpiredClean medExpiredClean)
    {
        startPage();
        List<MedExpiredClean> list = medExpiredCleanService.selectMedExpiredCleanList(medExpiredClean);
        return getDataTable(list);
    }

    /**
     * 导出过期药品清理列表
     */
    @PreAuthorize("@ss.hasPermi('system:expiredclean:export')")
    @Log(title = "过期药品清理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedExpiredClean medExpiredClean)
    {
        List<MedExpiredClean> list = medExpiredCleanService.selectMedExpiredCleanList(medExpiredClean);
        ExcelUtil<MedExpiredClean> util = new ExcelUtil<MedExpiredClean>(MedExpiredClean.class);
        util.exportExcel(response, list, "过期药品清理数据");
    }

    /**
     * 获取过期药品清理详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:expiredclean:query')")
    @GetMapping(value = "/{cleanId}")
    public AjaxResult getInfo(@PathVariable("cleanId") Long cleanId)
    {
        return success(medExpiredCleanService.selectMedExpiredCleanByCleanId(cleanId));
    }

    /**
     * 新增过期药品清理
     */
    @PreAuthorize("@ss.hasPermi('system:expiredclean:add')")
    @Log(title = "过期药品清理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedExpiredClean medExpiredClean)
    {
        return toAjax(medExpiredCleanService.insertMedExpiredClean(medExpiredClean));
    }

    /**
     * 修改过期药品清理
     */
    @PreAuthorize("@ss.hasPermi('system:expiredclean:edit')")
    @Log(title = "过期药品清理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedExpiredClean medExpiredClean)
    {
        return toAjax(medExpiredCleanService.updateMedExpiredClean(medExpiredClean));
    }

    /**
     * 确认清理（按清理数量扣减库存并登记库存流水）
     */
    @PreAuthorize("@ss.hasPermi('system:expiredclean:confirm')")
    @Log(title = "过期药品清理", businessType = BusinessType.UPDATE)
    @PutMapping("/confirm/{cleanId}")
    public AjaxResult confirm(@PathVariable("cleanId") Long cleanId, String remark)
    {
        return toAjax(medExpiredCleanService.confirmMedExpiredClean(cleanId, remark));
    }

    /**
     * 驳回清理申请
     */
    @PreAuthorize("@ss.hasPermi('system:expiredclean:confirm')")
    @Log(title = "过期药品清理", businessType = BusinessType.UPDATE)
    @PutMapping("/reject/{cleanId}")
    public AjaxResult reject(@PathVariable("cleanId") Long cleanId, String remark)
    {
        return toAjax(medExpiredCleanService.rejectMedExpiredClean(cleanId, remark));
    }

    /**
     * 删除过期药品清理
     */
    @PreAuthorize("@ss.hasPermi('system:expiredclean:remove')")
    @Log(title = "过期药品清理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{cleanIds}")
    public AjaxResult remove(@PathVariable Long[] cleanIds)
    {
        return toAjax(medExpiredCleanService.deleteMedExpiredCleanByCleanIds(cleanIds));
    }
}
