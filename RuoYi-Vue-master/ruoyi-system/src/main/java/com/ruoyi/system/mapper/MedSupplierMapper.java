package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MedSupplier;

/**
 * 供应商信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface MedSupplierMapper 
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
     * 删除供应商信息
     * 
     * @param supplierId 供应商信息主键
     * @return 结果
     */
    public int deleteMedSupplierBySupplierId(Long supplierId);

    /**
     * 批量删除供应商信息
     * 
     * @param supplierIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedSupplierBySupplierIds(Long[] supplierIds);

    /**
     * 校验供应商编码是否唯一
     * 
     * @param supplierCode 供应商编码
     * @return 供应商信息
     */
    public MedSupplier checkSupplierCodeUnique(String supplierCode);
}
