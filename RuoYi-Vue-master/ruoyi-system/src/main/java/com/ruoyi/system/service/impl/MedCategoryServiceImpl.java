package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.MedCategoryMapper;
import com.ruoyi.system.mapper.MedInfoMapper;
import com.ruoyi.system.domain.MedCategory;
import com.ruoyi.system.service.IMedCategoryService;

/**
 * 药品分类Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@Service
public class MedCategoryServiceImpl implements IMedCategoryService 
{
    @Autowired
    private MedCategoryMapper medCategoryMapper;

    @Autowired
    private MedInfoMapper medInfoMapper;

    /**
     * 查询药品分类
     * 
     * @param categoryId 药品分类主键
     * @return 药品分类
     */
    @Override
    public MedCategory selectMedCategoryByCategoryId(Long categoryId)
    {
        return medCategoryMapper.selectMedCategoryByCategoryId(categoryId);
    }

    /**
     * 查询药品分类列表
     * 
     * @param medCategory 药品分类
     * @return 药品分类
     */
    @Override
    public List<MedCategory> selectMedCategoryList(MedCategory medCategory)
    {
        return medCategoryMapper.selectMedCategoryList(medCategory);
    }

    /**
     * 新增药品分类
     * 
     * @param medCategory 药品分类
     * @return 结果
     */
    @Override
    public int insertMedCategory(MedCategory medCategory)
    {
        MedCategory parent = medCategoryMapper.selectMedCategoryByCategoryId(medCategory.getParentId());
        if (parent != null && "1".equals(parent.getStatus()))
        {
            throw new ServiceException("父分类已停用，不允许新增子分类");
        }
        if (parent != null)
        {
            medCategory.setAncestors(parent.getAncestors() + "," + parent.getCategoryId());
        }
        else
        {
            medCategory.setAncestors("0");
        }
        medCategory.setCreateBy(SecurityUtils.getUsername());
        return medCategoryMapper.insertMedCategory(medCategory);
    }

    /**
     * 修改药品分类
     * 
     * @param medCategory 药品分类
     * @return 结果
     */
    @Override
    public int updateMedCategory(MedCategory medCategory)
    {
        MedCategory newParent = medCategoryMapper.selectMedCategoryByCategoryId(medCategory.getParentId());
        if (newParent != null)
        {
            String newAncestors = newParent.getAncestors() + "," + newParent.getCategoryId();
            medCategory.setAncestors(newAncestors);
            updateChildrenAncestors(medCategory.getCategoryId(), newAncestors);
        }
        medCategory.setUpdateBy(SecurityUtils.getUsername());
        return medCategoryMapper.updateMedCategory(medCategory);
    }

    /**
     * 同步修改所有子分类的祖级列表
     */
    private void updateChildrenAncestors(Long categoryId, String newAncestors)
    {
        MedCategory query = new MedCategory();
        query.setParentId(categoryId);
        List<MedCategory> children = medCategoryMapper.selectMedCategoryList(query);
        for (MedCategory child : children)
        {
            String ancestors = newAncestors + "," + child.getCategoryId();
            MedCategory update = new MedCategory();
            update.setCategoryId(child.getCategoryId());
            update.setAncestors(ancestors);
            medCategoryMapper.updateMedCategory(update);
            updateChildrenAncestors(child.getCategoryId(), ancestors);
        }
    }

    /**
     * 批量删除药品分类
     * 
     * @param categoryIds 需要删除的药品分类主键
     * @return 结果
     */
    @Override
    public int deleteMedCategoryByCategoryIds(Long[] categoryIds)
    {
        for (Long categoryId : categoryIds)
        {
            checkCategoryAllowDelete(categoryId);
        }
        return medCategoryMapper.deleteMedCategoryByCategoryIds(categoryIds);
    }

    /**
     * 删除药品分类信息
     * 
     * @param categoryId 药品分类主键
     * @return 结果
     */
    @Override
    public int deleteMedCategoryByCategoryId(Long categoryId)
    {
        checkCategoryAllowDelete(categoryId);
        return medCategoryMapper.deleteMedCategoryByCategoryId(categoryId);
    }

    /**
     * 删除前的业务校验：存在子分类或已关联药品时不允许删除
     */
    private void checkCategoryAllowDelete(Long categoryId)
    {
        if (medCategoryMapper.hasChildByCategoryId(categoryId) > 0)
        {
            throw new ServiceException("存在子分类，不允许删除");
        }
        if (medInfoMapper.countMedByCategoryId(categoryId) > 0)
        {
            throw new ServiceException("该分类下已存在药品信息，不允许删除");
        }
    }
}
