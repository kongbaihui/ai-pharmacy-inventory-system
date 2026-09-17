package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedSupplier;

/**
 * 供应商信息Service接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface IMedSupplierService 
{
    /**
     * 查询供应商信息
     * 
     * @param supplierId 供应商信息主键
     * @return 供应商信息
     */
    public MedSupplier selectMedSupplierBySupplierId(Long supplierId);

    /**
     * 查询供应商信息列表
     * 
     * @param medSupplier 供应商信息
     * @return 供应商信息集合
     */
    public List<MedSupplier> selectMedSupplierList(MedSupplier medSupplier);

    /**
     * 新增供应商信息
     * 
     * @param medSupplier 供应商信息
     * @return 结果
     */
    public int insertMedSupplier(MedSupplier medSupplier);

    /**
     * 修改供应商信息
     * 
     * @param medSupplier 供应商信息
     * @return 结果
     */
    public int updateMedSupplier(MedSupplier medSupplier);

    /**
     * 批量删除供应商信息
     * 
     * @param supplierIds 需要删除的供应商信息主键集合
     * @return 结果
     */
    public int deleteMedSupplierBySupplierIds(Long[] supplierIds);

    /**
     * 删除供应商信息信息
     * 
     * @param supplierId 供应商信息主键
     * @return 结果
     */
    public int deleteMedSupplierBySupplierId(Long supplierId);
}
