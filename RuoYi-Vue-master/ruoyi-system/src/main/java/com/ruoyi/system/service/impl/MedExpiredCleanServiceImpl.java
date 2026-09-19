package com.ruoyi.system.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.MedExpiredCleanMapper;
import com.ruoyi.system.mapper.MedStockBatchMapper;
import com.ruoyi.system.domain.MedExpiredClean;
import com.ruoyi.system.domain.MedStockBatch;
import com.ruoyi.system.service.IMedExpiredCleanService;
import com.ruoyi.system.service.IMedStockService;

/**
 * 过期药品清理Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@Service
public class MedExpiredCleanServiceImpl implements IMedExpiredCleanService 
{
    @Autowired
    private MedExpiredCleanMapper medExpiredCleanMapper;

    @Autowired
    private MedStockBatchMapper medStockBatchMapper;

    @Autowired
    private IMedStockService medStockService;

    /**
     * 查询过期药品清理
     * 
     * @param cleanId 过期药品清理主键
     * @return 过期药品清理
     */
    @Override
    public MedExpiredClean selectMedExpiredCleanByCleanId(Long cleanId)
    {
        return medExpiredCleanMapper.selectMedExpiredCleanByCleanId(cleanId);
    }

    /**
     * 查询过期药品清理列表
     * 
     * @param medExpiredClean 过期药品清理
     * @return 过期药品清理
     */
    @Override
    public List<MedExpiredClean> selectMedExpiredCleanList(MedExpiredClean medExpiredClean)
    {
        return medExpiredCleanMapper.selectMedExpiredCleanList(medExpiredClean);
    }

    /**
     * 新增过期药品清理
     * 
     * @param medExpiredClean 过期药品清理
     * @return 结果
     */
    @Override
    public int insertMedExpiredClean(MedExpiredClean medExpiredClean)
    {
        MedStockBatch batch = checkCleanData(medExpiredClean);
        if (StringUtils.isEmpty(medExpiredClean.getBatchNo()))
        {
            medExpiredClean.setBatchNo(batch.getBatchNo());
        }
        if (medExpiredClean.getExpireDate() == null)
        {
            medExpiredClean.setExpireDate(batch.getExpireDate());
        }
        medExpiredClean.setCleanNo(generateCleanNo());
        medExpiredClean.setCleanStatus("0");
        medExpiredClean.setCleanUser(SecurityUtils.getUsername());
        medExpiredClean.setCleanTime(new Date());
        medExpiredClean.setCreateBy(SecurityUtils.getUsername());
        return medExpiredCleanMapper.insertMedExpiredClean(medExpiredClean);
    }

    /**
     * 修改过期药品清理
     * 
     * @param medExpiredClean 过期药品清理
     * @return 结果
     */
    @Override
    public int updateMedExpiredClean(MedExpiredClean medExpiredClean)
    {
        MedExpiredClean clean = medExpiredCleanMapper.selectMedExpiredCleanByCleanId(medExpiredClean.getCleanId());
        if (clean == null)
        {
            throw new ServiceException("清理记录不存在");
        }
        if (!"0".equals(clean.getCleanStatus()))
        {
            throw new ServiceException("清理记录已确认或已驳回，不允许修改");
        }
        checkCleanData(medExpiredClean);
        medExpiredClean.setUpdateBy(SecurityUtils.getUsername());
        return medExpiredCleanMapper.updateMedExpiredClean(medExpiredClean);
    }

    /**
     * 批量删除过期药品清理
     * 
     * @param cleanIds 需要删除的过期药品清理主键
     * @return 结果
     */
    @Override
    public int deleteMedExpiredCleanByCleanIds(Long[] cleanIds)
    {
        for (Long cleanId : cleanIds)
        {
            MedExpiredClean clean = medExpiredCleanMapper.selectMedExpiredCleanByCleanId(cleanId);
            if (clean != null && "1".equals(clean.getCleanStatus()))
            {
                throw new ServiceException("清理单【" + clean.getCleanNo() + "】已确认，不允许删除");
            }
        }
        return medExpiredCleanMapper.deleteMedExpiredCleanByCleanIds(cleanIds);
    }

    /**
     * 删除过期药品清理信息
     * 
     * @param cleanId 过期药品清理主键
     * @return 结果
     */
    @Override
    public int deleteMedExpiredCleanByCleanId(Long cleanId)
    {
        return medExpiredCleanMapper.deleteMedExpiredCleanByCleanId(cleanId);
    }

    /**
     * 确认清理：扣减库存与批次数量，并登记库存流水
     * 
     * @param cleanId 过期药品清理主键
     * @param remark 确认说明
     * @return 结果
     */
    @Override
    @Transactional
    public int confirmMedExpiredClean(Long cleanId, String remark)
    {
        MedExpiredClean clean = medExpiredCleanMapper.selectMedExpiredCleanByCleanId(cleanId);
        if (clean == null)
        {
            throw new ServiceException("清理记录不存在");
        }
        if (!"0".equals(clean.getCleanStatus()))
        {
            throw new ServiceException("清理单【" + clean.getCleanNo() + "】已处理，无需重复确认");
        }
        checkCleanData(clean);
        medStockService.adjustStock(clean.getMedId(), clean.getBatchId(), -clean.getCleanQty(), "5",
                clean.getCleanNo(), cleanId, "过期药品清理：" + (StringUtils.isEmpty(clean.getCleanReason()) ? "" : clean.getCleanReason()));
        if (clean.getBatchId() != null)
        {
            medStockBatchMapper.updateBatchRemainQty(clean.getBatchId(), -clean.getCleanQty());
            medStockBatchMapper.refreshBatchStatus();
        }
        MedExpiredClean update = new MedExpiredClean();
        update.setCleanId(cleanId);
        update.setCleanStatus("1");
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(new Date());
        update.setRemark(remark);
        update.setUpdateBy(SecurityUtils.getUsername());
        return medExpiredCleanMapper.updateMedExpiredClean(update);
    }

    /**
     * 驳回清理申请
     * 
     * @param cleanId 过期药品清理主键
     * @param remark 驳回说明
     * @return 结果
     */
    @Override
    public int rejectMedExpiredClean(Long cleanId, String remark)
    {
        MedExpiredClean clean = medExpiredCleanMapper.selectMedExpiredCleanByCleanId(cleanId);
        if (clean == null)
        {
            throw new ServiceException("清理记录不存在");
        }
        if (!"0".equals(clean.getCleanStatus()))
        {
            throw new ServiceException("清理单【" + clean.getCleanNo() + "】已处理，无需重复操作");
        }
        MedExpiredClean update = new MedExpiredClean();
        update.setCleanId(cleanId);
        update.setCleanStatus("2");
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(new Date());
        update.setRemark(remark);
        update.setUpdateBy(SecurityUtils.getUsername());
        return medExpiredCleanMapper.updateMedExpiredClean(update);
    }

    /**
     * 清理数据校验：批次存在、数量不超过批次剩余数量、非报损方式要求批次已过期
     */
    private MedStockBatch checkCleanData(MedExpiredClean medExpiredClean)
    {
        if (medExpiredClean.getBatchId() == null)
        {
            throw new ServiceException("请选择需要清理的药品批次");
        }
        if (medExpiredClean.getCleanQty() == null || medExpiredClean.getCleanQty() <= 0)
        {
            throw new ServiceException("清理数量必须大于 0");
        }
        MedStockBatch batch = medStockBatchMapper.selectMedStockBatchByBatchId(medExpiredClean.getBatchId());
        if (batch == null)
        {
            throw new ServiceException("药品批次不存在");
        }
        long remainQty = batch.getRemainQty() == null ? 0L : batch.getRemainQty();
        if (medExpiredClean.getCleanQty() > remainQty)
        {
            throw new ServiceException("清理数量不能大于批次剩余数量 " + remainQty);
        }
        boolean expired = "2".equals(batch.getBatchStatus())
                || (batch.getExpireDate() != null && batch.getExpireDate().before(new Date()));
        if (!expired && !"2".equals(medExpiredClean.getCleanType()))
        {
            throw new ServiceException("批号【" + batch.getBatchNo() + "】尚未过期，如需处理请选择报损方式");
        }
        medExpiredClean.setMedId(batch.getMedId());
        return batch;
    }

    /**
     * 生成清理单号，规则为 CL + 日期 + 4 位流水号
     */
    private String generateCleanNo()
    {
        String prefix = "CL" + new SimpleDateFormat("yyyyMMdd").format(new Date());
        String maxNo = medExpiredCleanMapper.selectMaxCleanNo(prefix);
        int sequence = 1;
        if (StringUtils.isNotEmpty(maxNo) && maxNo.length() > prefix.length())
        {
            sequence = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
        }
        return prefix + String.format("%04d", sequence);
    }
}
