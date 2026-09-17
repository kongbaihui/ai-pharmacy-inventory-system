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
     * 新增药品库存批次
     * 
     * @param medStockBatch 药品库存批次
     * @return 结果
     */
    public int insertMedStockBatch(MedStockBatch medStockBatch);

    /**
     * 修改药品库存批次
     * 
     * @param medStockBatch 药品库存批次
     * @return 结果
     */
    public int updateMedStockBatch(MedStockBatch medStockBatch);

    /**
     * 批量删除药品库存批次
     * 
     * @param batchIds 需要删除的药品库存批次主键集合
     * @return 结果
     */
    public int deleteMedStockBatchByBatchIds(Long[] batchIds);

    /**
     * 删除药品库存批次信息
     * 
     * @param batchId 药品库存批次主键
     * @return 结果
     */
    public int deleteMedStockBatchByBatchId(Long batchId);

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
