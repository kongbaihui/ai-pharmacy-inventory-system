package com.ruoyi.system.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.MedStockBatchMapper;
import com.ruoyi.system.domain.MedStockBatch;
import com.ruoyi.system.service.IMedStockBatchService;
import com.ruoyi.system.service.IMedStockService;

/**
 * 药品库存批次Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@Service
public class MedStockBatchServiceImpl implements IMedStockBatchService 
{
    @Autowired
    private MedStockBatchMapper medStockBatchMapper;

    @Autowired
    private IMedStockService medStockService;

    /**
     * 查询药品库存批次
     * 
     * @param batchId 药品库存批次主键
     * @return 药品库存批次
     */
    @Override
    public MedStockBatch selectMedStockBatchByBatchId(Long batchId)
    {
        return medStockBatchMapper.selectMedStockBatchByBatchId(batchId);
    }

    /**
     * 查询药品库存批次列表
     * 
     * @param medStockBatch 药品库存批次
     * @return 药品库存批次
     */
    @Override
    public List<MedStockBatch> selectMedStockBatchList(MedStockBatch medStockBatch)
    {
        return medStockBatchMapper.selectMedStockBatchList(medStockBatch);
    }

    /**
     * 维护批次效期信息
     * 
     * 批次数量与库存总量只能由入库、出库、盘点、过期清理等业务变更，
     * 本方法只允许修改生产日期、有效期、供应商与备注，
     * 避免绕过库存变更入口直接改数量导致库存总量与批次之和对不上。
     * 
     * @param medStockBatch 药品库存批次
     * @return 结果
     */
    @Override
    public int updateBatchExpiry(MedStockBatch medStockBatch)
    {
        if (medStockBatch.getBatchId() == null)
        {
            throw new ServiceException("批次不能为空");
        }
        MedStockBatch batch = medStockBatchMapper.selectMedStockBatchByBatchId(medStockBatch.getBatchId());
        if (batch == null)
        {
            throw new ServiceException("批次不存在");
        }
        if (medStockBatch.getExpireDate() == null)
        {
            medStockBatch.setExpireDate(batch.getExpireDate());
        }
        if (medStockBatch.getProduceDate() != null && medStockBatch.getExpireDate().before(medStockBatch.getProduceDate()))
        {
            throw new ServiceException("有效期不能早于生产日期");
        }
        // 只提交允许修改的字段，批号、批次数量、剩余数量等字段不参与更新
        MedStockBatch update = new MedStockBatch();
        update.setBatchId(medStockBatch.getBatchId());
        update.setProduceDate(medStockBatch.getProduceDate());
        update.setExpireDate(medStockBatch.getExpireDate());
        update.setSupplierId(medStockBatch.getSupplierId());
        update.setRemark(medStockBatch.getRemark());
        update.setUpdateBy(SecurityUtils.getUsername());
        int rows = medStockBatchMapper.updateMedStockBatch(update);
        medStockBatchMapper.refreshBatchStatus();
        return rows;
    }

    /**
     * 按批次剩余数量重算库存总量
     * 
     * @return 重算结果
     */
    @Override
    @Transactional
    public Map<String, Object> recalcStockQty()
    {
        List<MedStockBatch> diffList = medStockBatchMapper.selectStockQtyDiffList();
        int fixedCount = 0;
        long totalDiffQty = 0L;
        if (diffList != null)
        {
            for (MedStockBatch item : diffList)
            {
                long diffQty = item.getDiffQty() == null ? 0L : item.getDiffQty();
                if (diffQty == 0L)
                {
                    continue;
                }
                medStockService.adjustStock(item.getMedId(), null, diffQty, "4", "库存总量重算", null,
                        "按批次剩余数量重算库存总量");
                fixedCount++;
                totalDiffQty += diffQty;
            }
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("diffCount", diffList == null ? 0 : diffList.size());
        result.put("fixedCount", fixedCount);
        result.put("totalDiffQty", totalDiffQty);
        return result;
    }

    /**
     * 刷新批次效期状态：按药品近效期预警天数重新判定正常、临期、过期
     * 
     * @return 更新的批次数
     */
    @Override
    public int refreshBatchStatus()
    {
        return medStockBatchMapper.refreshBatchStatus();
    }

    /**
     * 统计临期与过期药品数量
     * 
     * @return 统计数据
     */
    @Override
    public Map<String, Object> selectExpirySummary()
    {
        return medStockBatchMapper.selectExpirySummary();
    }

}
