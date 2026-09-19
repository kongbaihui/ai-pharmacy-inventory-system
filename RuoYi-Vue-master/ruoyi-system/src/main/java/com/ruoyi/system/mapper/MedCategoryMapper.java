package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MedCategory;

/**
 * 药品分类Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface MedCategoryMapper 
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
     * 删除药品分类
     * 
     * @param categoryId 药品分类主键
     * @return 结果
     */
    public int deleteMedCategoryByCategoryId(Long categoryId);

    /**
     * 批量删除药品分类
     * 
     * @param categoryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 查询是否存在子分类
     * 
     * @param categoryId 药品分类主键
     * @return 子分类数量
     */
    public int hasChildByCategoryId(Long categoryId);
}
