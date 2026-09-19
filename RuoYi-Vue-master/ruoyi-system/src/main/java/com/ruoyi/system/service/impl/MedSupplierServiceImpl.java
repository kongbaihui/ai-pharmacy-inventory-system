package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.MedSupplierMapper;
import com.ruoyi.system.domain.MedSupplier;
import com.ruoyi.system.service.IMedSupplierService;

/**
 * 供应商信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@Service
public class MedSupplierServiceImpl implements IMedSupplierService 
{
    @Autowired
    private MedSupplierMapper medSupplierMapper;

    /**
     * 查询供应商信息
     * 
     * @param supplierId 供应商信息主键
     * @return 供应商信息
     */
    @Override
    public MedSupplier selectMedSupplierBySupplierId(Long supplierId)
    {
        return medSupplierMapper.selectMedSupplierBySupplierId(supplierId);
    }

    /**
     * 查询供应商信息列表
     * 
     * @param medSupplier 供应商信息
     * @return 供应商信息
     */
    @Override
    public List<MedSupplier> selectMedSupplierList(MedSupplier medSupplier)
    {
        return medSupplierMapper.selectMedSupplierList(medSupplier);
    }

    /**
     * 新增供应商信息
     * 
     * @param medSupplier 供应商信息
     * @return 结果
     */
    @Override
    public int insertMedSupplier(MedSupplier medSupplier)
    {
        checkSupplierCodeUnique(medSupplier);
        medSupplier.setCreateBy(SecurityUtils.getUsername());
        return medSupplierMapper.insertMedSupplier(medSupplier);
    }

    /**
     * 修改供应商信息
     * 
     * @param medSupplier 供应商信息
     * @return 结果
     */
    @Override
    public int updateMedSupplier(MedSupplier medSupplier)
    {
        checkSupplierCodeUnique(medSupplier);
        medSupplier.setUpdateBy(SecurityUtils.getUsername());
        return medSupplierMapper.updateMedSupplier(medSupplier);
    }

    /**
     * 批量删除供应商信息
     * 
     * @param supplierIds 需要删除的供应商信息主键
     * @return 结果
     */
    @Override
    public int deleteMedSupplierBySupplierIds(Long[] supplierIds)
    {
        return medSupplierMapper.deleteMedSupplierBySupplierIds(supplierIds);
    }

    /**
     * 删除供应商信息信息
     * 
     * @param supplierId 供应商信息主键
     * @return 结果
     */
    @Override
    public int deleteMedSupplierBySupplierId(Long supplierId)
    {
        return medSupplierMapper.deleteMedSupplierBySupplierId(supplierId);
    }

    /**
     * 校验供应商编码唯一性
     */
    private void checkSupplierCodeUnique(MedSupplier medSupplier)
    {
        MedSupplier info = medSupplierMapper.checkSupplierCodeUnique(medSupplier.getSupplierCode());
        if (info != null && !info.getSupplierId().equals(medSupplier.getSupplierId()))
        {
            throw new ServiceException("供应商编码【" + medSupplier.getSupplierCode() + "】已存在");
        }
    }
}
