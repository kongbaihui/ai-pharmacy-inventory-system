package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MedStockBatch;

/**
 * 药品库存批次Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface MedStockBatchMapper 
{
    /**
     * 查询药品库存批次
     * 
     * @param batchId 药品库存批次主键
     * @return 药品库存批次
     */
    public MedStockBatch selectMedStockBatchByBatchId(Long batchId);

    /**
     * 查询药品库存批次列表（支持临期、过期筛选）
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
     * 删除药品库存批次
     * 
     * @param batchId 药品库存批次主键
     * @return 结果
     */
    public int deleteMedStockBatchByBatchId(Long batchId);

    /**
     * 批量删除药品库存批次
     * 
     * @param batchIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedStockBatchByBatchIds(Long[] batchIds);

    /**
     * 变更批次剩余数量
     * 
     * @param batchId 批次主键
     * @param changeQty 变动数量（正数为增加，负数为减少）
     * @return 结果
     */
    public int updateBatchRemainQty(@Param("batchId") Long batchId, @Param("changeQty") Long changeQty);

    /**
     * 重新计算批次状态（正常、临期、过期、已清理）
     * 
     * @return 结果
     */
    public int refreshBatchStatus();

    /**
     * 统计临期与过期药品数量
     * 
     * @return 统计数据
     */
    public Map<String, Object> selectExpirySummary();
}
