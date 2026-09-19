package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedStock;
import com.ruoyi.system.domain.MedStockFlow;

/**
 * 库存变更Service接口
 * 
 * 说明：入库、出库、退库、盘点调整、过期清理等所有会改变库存数量的业务，
 * 统一调用本接口完成库存变更与库存流水登记，保证库存数据一致。
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface IMedStockService 
{
    /**
     * 查询药品当前库存数量
     * 
     * @param medId 药品ID
     * @return 库存数量
     */
    public Long selectStockQtyByMedId(Long medId);

    /**
     * 查询药品库存
     * 
     * @param medId 药品ID
     * @return 药品库存
     */
    public MedStock selectMedStockByMedId(Long medId);

    /**
     * 查询库存列表
     * 
     * @param medStock 药品库存
     * @return 药品库存集合
     */
    public List<MedStock> selectMedStockList(MedStock medStock);

    /**
     * 变更库存数量并登记库存流水（统一库存变更入口）
     * 
     * @param medId 药品ID
     * @param batchId 批次ID
     * @param changeQty 变动数量（正数为增加，负数为减少）
     * @param flowType 业务类型（1入库 2出库 3退库 4盘点调整 5过期清理）
     * @param bizNo 业务单号
     * @param bizId 业务主键
     * @param remark 备注
     * @return 结果
     */
    public int adjustStock(Long medId, Long batchId, Long changeQty, String flowType, String bizNo, Long bizId, String remark);

    /**
     * 查询库存流水列表
     * 
     * @param medStockFlow 库存流水
     * @return 库存流水集合
     */
    public List<MedStockFlow> selectMedStockFlowList(MedStockFlow medStockFlow);
}
