package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MedStock;
import com.ruoyi.system.domain.MedStockFlow;

/**
 * 库存与库存流水Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface MedStockMapper 
{
    /**
     * 查询药品库存
     * 
     * @param medId 药品ID
     * @return 药品库存
     */
    public MedStock selectMedStockByMedId(Long medId);

    /**
     * 查询药品库存（加行锁，用于库存数量变更）
     * 
     * @param medId 药品ID
     * @return 药品库存
     */
    public MedStock selectMedStockByMedIdForUpdate(Long medId);

    /**
     * 查询药品库存列表
     * 
     * @param medStock 药品库存
     * @return 药品库存集合
     */
    public List<MedStock> selectMedStockList(MedStock medStock);

    /**
     * 新增药品库存
     * 
     * @param medStock 药品库存
     * @return 结果
     */
    public int insertMedStock(MedStock medStock);

    /**
     * 更新药品库存数量
     * 
     * @param medStock 药品库存
     * @return 结果
     */
    public int updateMedStockQty(MedStock medStock);

    /**
     * 新增库存流水
     * 
     * @param medStockFlow 库存流水
     * @return 结果
     */
    public int insertMedStockFlow(MedStockFlow medStockFlow);

    /**
     * 查询库存流水列表
     * 
     * @param medStockFlow 库存流水
     * @return 库存流水集合
     */
    public List<MedStockFlow> selectMedStockFlowList(MedStockFlow medStockFlow);
}
