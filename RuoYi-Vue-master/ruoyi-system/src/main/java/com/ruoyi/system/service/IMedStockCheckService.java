package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedStockCheck;
import com.ruoyi.system.domain.MedStockCheckItem;

/**
 * 库存盘点Service接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface IMedStockCheckService 
{
    /**
     * 查询库存盘点
     * 
     * @param checkId 库存盘点主键
     * @return 库存盘点
     */
    public MedStockCheck selectMedStockCheckByCheckId(Long checkId);

    /**
     * 查询库存盘点列表
     * 
     * @param medStockCheck 库存盘点
     * @return 库存盘点集合
     */
    public List<MedStockCheck> selectMedStockCheckList(MedStockCheck medStockCheck);

    /**
     * 查询盘点明细列表
     * 
     * @param medStockCheckItem 盘点明细
     * @return 盘点明细集合
     */
    public List<MedStockCheckItem> selectMedStockCheckItemList(MedStockCheckItem medStockCheckItem);

    /**
     * 按当前批次库存生成盘点明细（账面数量）
     * 
     * @param medStockCheck 库存盘点
     * @return 盘点明细集合
     */
    public List<MedStockCheckItem> selectBookStockItemList(MedStockCheck medStockCheck);

    /**
     * 新增库存盘点
     * 
     * @param medStockCheck 库存盘点
     * @return 结果
     */
    public int insertMedStockCheck(MedStockCheck medStockCheck);

    /**
     * 修改库存盘点
     * 
     * @param medStockCheck 库存盘点
     * @return 结果
     */
    public int updateMedStockCheck(MedStockCheck medStockCheck);

    /**
     * 批量删除库存盘点
     * 
     * @param checkIds 需要删除的库存盘点主键集合
     * @return 结果
     */
    public int deleteMedStockCheckByCheckIds(Long[] checkIds);

    /**
     * 删除库存盘点信息
     * 
     * @param checkId 库存盘点主键
     * @return 结果
     */
    public int deleteMedStockCheckByCheckId(Long checkId);

    /**
     * 盘点审核：按盈亏数量调整库存并登记库存流水
     * 
     * @param checkId 库存盘点主键
     * @return 调整的明细条数
     */
    public int auditMedStockCheck(Long checkId);
}
