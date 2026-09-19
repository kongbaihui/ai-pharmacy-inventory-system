package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.MedStockBatchMapper;
import com.ruoyi.system.mapper.MedStockWarnMapper;
import com.ruoyi.system.domain.MedStockWarn;
import com.ruoyi.system.service.IMedStockWarnService;

/**
 * 库存预警Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@Service
public class MedStockWarnServiceImpl implements IMedStockWarnService 
{
    @Autowired
    private MedStockWarnMapper medStockWarnMapper;

    @Autowired
    private MedStockBatchMapper medStockBatchMapper;

    /**
     * 查询库存预警
     * 
     * @param warnId 库存预警主键
     * @return 库存预警
     */
    @Override
    public MedStockWarn selectMedStockWarnByWarnId(Long warnId)
    {
        return medStockWarnMapper.selectMedStockWarnByWarnId(warnId);
    }

    /**
     * 查询库存预警列表
     * 
     * @param medStockWarn 库存预警
     * @return 库存预警
     */
    @Override
    public List<MedStockWarn> selectMedStockWarnList(MedStockWarn medStockWarn)
    {
        return medStockWarnMapper.selectMedStockWarnList(medStockWarn);
    }

    /**
     * 扫描库存与效期数据，生成未处理的预警记录
     * 
     * 库存不足与库存积压依据药品信息中的库存上下限判断，
     * 近效期与已过期依据批次有效期和药品信息的近效期预警天数判断。
     * 
     * @return 本次新增预警数量
     */
    @Override
    @Transactional
    public int scanStockWarn()
    {
        medStockBatchMapper.refreshBatchStatus();
        List<MedStockWarn> sourceList = medStockWarnMapper.selectStockWarnSourceList();
        if (sourceList == null || sourceList.isEmpty())
        {
            return 0;
        }
        List<MedStockWarn> insertList = new ArrayList<MedStockWarn>();
        for (MedStockWarn warn : sourceList)
        {
            if (medStockWarnMapper.countUnhandleWarn(warn.getMedId(), warn.getBatchId(), warn.getWarnType()) > 0)
            {
                continue;
            }
            warn.setWarnTime(new Date());
            warn.setHandleStatus("0");
            warn.setCreateBy(SecurityUtils.getUsername());
            insertList.add(warn);
        }
        if (insertList.isEmpty())
        {
            return 0;
        }
        medStockWarnMapper.batchMedStockWarn(insertList);
        return insertList.size();
    }

    /**
     * 新增库存预警
     * 
     * @param medStockWarn 库存预警
     * @return 结果
     */
    @Override
    public int insertMedStockWarn(MedStockWarn medStockWarn)
    {
        if (medStockWarn.getWarnTime() == null)
        {
            medStockWarn.setWarnTime(new Date());
        }
        if (StringUtils.isEmpty(medStockWarn.getHandleStatus()))
        {
            medStockWarn.setHandleStatus("0");
        }
        medStockWarn.setCreateBy(SecurityUtils.getUsername());
        return medStockWarnMapper.insertMedStockWarn(medStockWarn);
    }

    /**
     * 修改库存预警
     * 
     * @param medStockWarn 库存预警
     * @return 结果
     */
    @Override
    public int updateMedStockWarn(MedStockWarn medStockWarn)
    {
        return medStockWarnMapper.updateMedStockWarn(medStockWarn);
    }

    /**
     * 处理预警（标记已处理或已忽略）
     * 
     * @param medStockWarn 库存预警
     * @return 结果
     */
    @Override
    public int handleMedStockWarn(MedStockWarn medStockWarn)
    {
        if (medStockWarn.getWarnId() == null && (medStockWarn.getWarnIds() == null || medStockWarn.getWarnIds().length == 0))
        {
            throw new ServiceException("请选择需要处理的预警记录");
        }
        if (StringUtils.isEmpty(medStockWarn.getHandleStatus()))
        {
            medStockWarn.setHandleStatus("1");
        }
        medStockWarn.setHandleUser(SecurityUtils.getUsername());
        medStockWarn.setHandleTime(new Date());
        if (medStockWarn.getWarnIds() != null && medStockWarn.getWarnIds().length > 0)
        {
            return medStockWarnMapper.batchUpdateHandleStatus(medStockWarn);
        }
        MedStockWarn warn = medStockWarnMapper.selectMedStockWarnByWarnId(medStockWarn.getWarnId());
        if (warn == null)
        {
            throw new ServiceException("预警记录不存在");
        }
        if (!"0".equals(warn.getHandleStatus()))
        {
            throw new ServiceException("该预警记录已处理，无需重复处理");
        }
        return medStockWarnMapper.updateMedStockWarn(medStockWarn);
    }

    /**
     * 批量删除库存预警
     * 
     * @param warnIds 需要删除的库存预警主键
     * @return 结果
     */
    @Override
    public int deleteMedStockWarnByWarnIds(Long[] warnIds)
    {
        return medStockWarnMapper.deleteMedStockWarnByWarnIds(warnIds);
    }

    /**
     * 删除库存预警信息
     * 
     * @param warnId 库存预警主键
     * @return 结果
     */
    @Override
    public int deleteMedStockWarnByWarnId(Long warnId)
    {
        return medStockWarnMapper.deleteMedStockWarnByWarnId(warnId);
    }

    /**
     * 统计各类别预警数量（供库存预警与数据看板使用）
     * 
     * @return 统计数据
     */
    @Override
    public Map<String, Object> selectWarnSummary()
    {
        return medStockWarnMapper.selectWarnSummary();
    }
}
