package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.MedInfoMapper;
import com.ruoyi.system.domain.MedInfo;
import com.ruoyi.system.service.IMedInfoService;

/**
 * 药品信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@Service
public class MedInfoServiceImpl implements IMedInfoService 
{
    @Autowired
    private MedInfoMapper medInfoMapper;

    /**
     * 查询药品信息
     * 
     * @param medId 药品信息主键
     * @return 药品信息
     */
    @Override
    public MedInfo selectMedInfoByMedId(Long medId)
    {
        return medInfoMapper.selectMedInfoByMedId(medId);
    }

    /**
     * 查询药品信息列表
     * 
     * @param medInfo 药品信息
     * @return 药品信息
     */
    @Override
    public List<MedInfo> selectMedInfoList(MedInfo medInfo)
    {
        return medInfoMapper.selectMedInfoList(medInfo);
    }

    /**
     * 新增药品信息
     * 
     * @param medInfo 药品信息
     * @return 结果
     */
    @Override
    public int insertMedInfo(MedInfo medInfo)
    {
        checkMedInfo(medInfo);
        medInfo.setCreateBy(SecurityUtils.getUsername());
        return medInfoMapper.insertMedInfo(medInfo);
    }

    /**
     * 修改药品信息
     * 
     * @param medInfo 药品信息
     * @return 结果
     */
    @Override
    public int updateMedInfo(MedInfo medInfo)
    {
        checkMedInfo(medInfo);
        medInfo.setUpdateBy(SecurityUtils.getUsername());
        return medInfoMapper.updateMedInfo(medInfo);
    }

    /**
     * 批量删除药品信息
     * 
     * @param medIds 需要删除的药品信息主键
     * @return 结果
     */
    @Override
    public int deleteMedInfoByMedIds(Long[] medIds)
    {
        return medInfoMapper.deleteMedInfoByMedIds(medIds);
    }

    /**
     * 删除药品信息信息
     * 
     * @param medId 药品信息主键
     * @return 结果
     */
    @Override
    public int deleteMedInfoByMedId(Long medId)
    {
        return medInfoMapper.deleteMedInfoByMedId(medId);
    }

    /**
     * 保存前的业务校验：编码唯一、库存上下限合法
     */
    private void checkMedInfo(MedInfo medInfo)
    {
        MedInfo info = medInfoMapper.checkMedCodeUnique(medInfo.getMedCode());
        if (info != null && !info.getMedId().equals(medInfo.getMedId()))
        {
            throw new ServiceException("药品编码【" + medInfo.getMedCode() + "】已存在");
        }
        if (medInfo.getStockMin() == null || medInfo.getStockMax() == null)
        {
            throw new ServiceException("库存下限与库存上限不能为空，否则无法进行库存预警");
        }
        if (medInfo.getStockMin() > medInfo.getStockMax())
        {
            throw new ServiceException("库存下限不能大于库存上限");
        }
    }
}
