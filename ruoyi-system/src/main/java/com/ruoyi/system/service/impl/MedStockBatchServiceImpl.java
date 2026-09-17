package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.MedStockBatchMapper;
import com.ruoyi.system.domain.MedStockBatch;
import com.ruoyi.system.service.IMedStockBatchService;

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
     * 新增药品库存批次
     * 
     * @param medStockBatch 药品库存批次
     * @return 结果
     */
    @Override
    public int insertMedStockBatch(MedStockBatch medStockBatch)
    {
        checkBatch(medStockBatch);
        if (medStockBatch.getInTime() == null)
        {
            medStockBatch.setInTime(new Date());
        }
        if (medStockBatch.getRemainQty() == null)
        {
            medStockBatch.setRemainQty(medStockBatch.getBatchQty());
        }
        medStockBatch.setCreateBy(SecurityUtils.getUsername());
        int rows = medStockBatchMapper.insertMedStockBatch(medStockBatch);
        medStockBatchMapper.refreshBatchStatus();
        return rows;
    }

    /**
     * 修改药品库存批次
     * 
     * @param medStockBatch 药品库存批次
     * @return 结果
     */
    @Override
    public int updateMedStockBatch(MedStockBatch medStockBatch)
    {
        checkBatch(medStockBatch);
        medStockBatch.setUpdateBy(SecurityUtils.getUsername());
        int rows = medStockBatchMapper.updateMedStockBatch(medStockBatch);
        medStockBatchMapper.refreshBatchStatus();
        return rows;
    }

    /**
     * 批量删除药品库存批次
     * 
     * @param batchIds 需要删除的药品库存批次主键
     * @return 结果
     */
    @Override
    public int deleteMedStockBatchByBatchIds(Long[] batchIds)
    {
        for (Long batchId : batchIds)
        {
            MedStockBatch batch = medStockBatchMapper.selectMedStockBatchByBatchId(batchId);
            if (batch != null && batch.getRemainQty() != null && batch.getRemainQty() > 0)
            {
                throw new ServiceException("批号【" + batch.getBatchNo() + "】仍有剩余库存，不允许删除");
            }
        }
        return medStockBatchMapper.deleteMedStockBatchByBatchIds(batchIds);
    }

    /**
     * 删除药品库存批次信息
     * 
     * @param batchId 药品库存批次主键
     * @return 结果
     */
    @Override
    public int deleteMedStockBatchByBatchId(Long batchId)
    {
        return medStockBatchMapper.deleteMedStockBatchByBatchId(batchId);
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

    /**
     * 批次数据校验：有效期必须晚于生产日期
     */
    private void checkBatch(MedStockBatch medStockBatch)
    {
        if (medStockBatch.getExpireDate() == null)
        {
            throw new ServiceException("有效期不能为空，否则无法进行效期管理");
        }
        if (medStockBatch.getProduceDate() != null && medStockBatch.getExpireDate().before(medStockBatch.getProduceDate()))
        {
            throw new ServiceException("有效期不能早于生产日期");
        }
    }
}
