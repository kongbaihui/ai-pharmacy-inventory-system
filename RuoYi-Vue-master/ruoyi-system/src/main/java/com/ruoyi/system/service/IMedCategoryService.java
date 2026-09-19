package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedCategory;

/**
 * 药品分类Service接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface IMedCategoryService 
{
    /**
     * 查询药品分类
     * 
     * @param categoryId 药品分类主键
     * @return 药品分类
     */
    public MedCategory selectMedCategoryByCategoryId(Long categoryId);

    /**
     * 查询药品分类列表
     * 
     * @param medCategory 药品分类
     * @return 药品分类集合
     */
    public List<MedCategory> selectMedCategoryList(MedCategory medCategory);

    /**
     * 新增药品分类
     * 
     * @param medCategory 药品分类
     * @return 结果
     */
    public int insertMedCategory(MedCategory medCategory);

    /**
     * 修改药品分类
     * 
     * @param medCategory 药品分类
     * @return 结果
     */
    public int updateMedCategory(MedCategory medCategory);

    /**
     * 批量删除药品分类
     * 
     * @param categoryIds 需要删除的药品分类主键集合
     * @return 结果
     */
    public int deleteMedCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 删除药品分类信息
     * 
     * @param categoryId 药品分类主键
     * @return 结果
     */
    public int deleteMedCategoryByCategoryId(Long categoryId);
}
