package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.MedStockBatch;

/**
 * 药品库存批次Service接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface IMedStockBatchService 
{
    /**
     * 查询药品库存批次
     * 
     * @param batchId 药品库存批次主键
     * @return 药品库存批次
     */
    public MedStockBatch selectMedStockBatchByBatchId(Long batchId);

    /**
     * 查询药品库存批次列表
     * 
     * @param medStockBatch 药品库存批次
     * @return 药品库存批次集合
     */
    public List<MedStockBatch> selectMedStockBatchList(MedStockBatch medStockBatch);

    /**
     * 维护批次效期信息（仅允许修改生产日期、有效期、供应商、备注，不允许修改数量）
     * 
     * @param medStockBatch 药品库存批次
     * @return 结果
     */
    public int updateBatchExpiry(MedStockBatch medStockBatch);

    /**
     * 按批次剩余数量重算库存总量
     * 
     * 批次数量是库存的明细，库存总量是汇总，正常情况下两者应保持一致。
     * 本方法用于修正历史数据：逐种药品比较批次剩余数量之和与库存总量，
     * 存在差异时通过统一库存变更服务调整库存并写入流水。
     * 
     * @return 重算结果（差异条数、修正条数、调整数量合计）
     */
    public Map<String, Object> recalcStockQty();

    /**
     * 刷新批次效期状态：按药品近效期预警天数重新判定正常、临期、过期
     * 
     * @return 更新的批次数
     */
    public int refreshBatchStatus();

    /**
     * 统计临期与过期药品数量
     * 
     * @return 统计数据
     */
    public Map<String, Object> selectExpirySummary();
}
