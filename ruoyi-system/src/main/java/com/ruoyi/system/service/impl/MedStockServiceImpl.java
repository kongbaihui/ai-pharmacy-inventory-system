package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.MedStockMapper;
import com.ruoyi.system.domain.MedStock;
import com.ruoyi.system.domain.MedStockFlow;
import com.ruoyi.system.service.IMedStockService;

/**
 * 库存变更Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@Service
public class MedStockServiceImpl implements IMedStockService 
{
    @Autowired
    private MedStockMapper medStockMapper;

    /**
     * 查询药品当前库存数量
     * 
     * @param medId 药品ID
     * @return 库存数量
     */
    @Override
    public Long selectStockQtyByMedId(Long medId)
    {
        MedStock stock = medStockMapper.selectMedStockByMedId(medId);
        return stock == null || stock.getTotalQty() == null ? 0L : stock.getTotalQty();
    }

    /**
     * 查询药品库存
     * 
     * @param medId 药品ID
     * @return 药品库存
     */
    @Override
    public MedStock selectMedStockByMedId(Long medId)
    {
        return medStockMapper.selectMedStockByMedId(medId);
    }

    /**
     * 查询库存列表
     * 
     * @param medStock 药品库存
     * @return 药品库存集合
     */
    @Override
    public List<MedStock> selectMedStockList(MedStock medStock)
    {
        return medStockMapper.selectMedStockList(medStock);
    }

    /**
     * 变更库存数量并登记库存流水（统一库存变更入口）
     * 
     * @param medId 药品ID
     * @param batchId 批次ID
     * @param changeQty 变动数量
     * @param flowType 业务类型
     * @param bizNo 业务单号
     * @param bizId 业务主键
     * @param remark 备注
     * @return 结果
     */
    @Override
    @Transactional
    public int adjustStock(Long medId, Long batchId, Long changeQty, String flowType, String bizNo, Long bizId, String remark)
    {
        if (medId == null)
        {
            throw new ServiceException("药品不能为空，无法变更库存");
        }
        if (changeQty == null || changeQty == 0L)
        {
            return 0;
        }
        MedStock stock = medStockMapper.selectMedStockByMedIdForUpdate(medId);
        if (stock == null)
        {
            if (changeQty < 0)
            {
                throw new ServiceException("该药品尚无库存记录，无法减少库存");
            }
            stock = new MedStock();
            stock.setMedId(medId);
            stock.setTotalQty(0L);
            stock.setLockQty(0L);
            stock.setCreateBy(SecurityUtils.getUsername());
            medStockMapper.insertMedStock(stock);
        }
        long beforeQty = stock.getTotalQty() == null ? 0L : stock.getTotalQty();
        long afterQty = beforeQty + changeQty;
        if (afterQty < 0)
        {
            throw new ServiceException("库存数量不足，当前库存为 " + beforeQty + "，本次需要减少 " + Math.abs(changeQty));
        }
        stock.setTotalQty(afterQty);
        if ("1".equals(flowType))
        {
            stock.setLastInTime(new Date());
        }
        else if ("2".equals(flowType))
        {
            stock.setLastOutTime(new Date());
        }
        stock.setUpdateBy(SecurityUtils.getUsername());
        medStockMapper.updateMedStockQty(stock);

        MedStockFlow flow = new MedStockFlow();
        flow.setMedId(medId);
        flow.setBatchId(batchId);
        flow.setFlowType(flowType);
        flow.setChangeQty(changeQty);
        flow.setBeforeQty(beforeQty);
        flow.setAfterQty(afterQty);
        flow.setBizNo(bizNo);
        flow.setBizId(bizId);
        flow.setOperator(SecurityUtils.getUsername());
        flow.setFlowTime(new Date());
        flow.setRemark(remark);
        return medStockMapper.insertMedStockFlow(flow);
    }

    /**
     * 查询库存流水列表
     * 
     * @param medStockFlow 库存流水
     * @return 库存流水集合
     */
    @Override
    public List<MedStockFlow> selectMedStockFlowList(MedStockFlow medStockFlow)
    {
        return medStockMapper.selectMedStockFlowList(medStockFlow);
    }
}
