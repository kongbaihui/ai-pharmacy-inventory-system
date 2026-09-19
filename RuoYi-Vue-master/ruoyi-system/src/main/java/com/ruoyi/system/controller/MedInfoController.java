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
import com.ruoyi.system.domain.MedInfo;
import com.ruoyi.system.service.IMedInfoService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 药品信息Controller
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/system/info")
public class MedInfoController extends BaseController
{
    @Autowired
    private IMedInfoService medInfoService;

    /**
     * 查询药品信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedInfo medInfo)
    {
        startPage();
        List<MedInfo> list = medInfoService.selectMedInfoList(medInfo);
        return getDataTable(list);
    }

    /**
     * 查询药品下拉选项（供批次、盘点、预警等模块选择药品使用）
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        MedInfo medInfo = new MedInfo();
        medInfo.setStatus("0");
        return success(medInfoService.selectMedInfoList(medInfo));
    }

    /**
     * 导出药品信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:info:export')")
    @Log(title = "药品信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedInfo medInfo)
    {
        List<MedInfo> list = medInfoService.selectMedInfoList(medInfo);
        ExcelUtil<MedInfo> util = new ExcelUtil<MedInfo>(MedInfo.class);
        util.exportExcel(response, list, "药品信息数据");
    }

    /**
     * 获取药品信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:info:query')")
    @GetMapping(value = "/{medId}")
    public AjaxResult getInfo(@PathVariable("medId") Long medId)
    {
        return success(medInfoService.selectMedInfoByMedId(medId));
    }

    /**
     * 新增药品信息
     */
    @PreAuthorize("@ss.hasPermi('system:info:add')")
    @Log(title = "药品信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedInfo medInfo)
    {
        return toAjax(medInfoService.insertMedInfo(medInfo));
    }

    /**
     * 修改药品信息
     */
    @PreAuthorize("@ss.hasPermi('system:info:edit')")
    @Log(title = "药品信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedInfo medInfo)
    {
        return toAjax(medInfoService.updateMedInfo(medInfo));
    }

    /**
     * 删除药品信息
     */
    @PreAuthorize("@ss.hasPermi('system:info:remove')")
    @Log(title = "药品信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{medIds}")
    public AjaxResult remove(@PathVariable Long[] medIds)
    {
        return toAjax(medInfoService.deleteMedInfoByMedIds(medIds));
    }
}
