package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedInfo;

/**
 * 药品信息Service接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface IMedInfoService 
{
    /**
     * 查询药品信息
     * 
     * @param medId 药品信息主键
     * @return 药品信息
     */
    public MedInfo selectMedInfoByMedId(Long medId);

    /**
     * 查询药品信息列表
     * 
     * @param medInfo 药品信息
     * @return 药品信息集合
     */
    public List<MedInfo> selectMedInfoList(MedInfo medInfo);

    /**
     * 新增药品信息
     * 
     * @param medInfo 药品信息
     * @return 结果
     */
    public int insertMedInfo(MedInfo medInfo);

    /**
     * 修改药品信息
     * 
     * @param medInfo 药品信息
     * @return 结果
     */
    public int updateMedInfo(MedInfo medInfo);

    /**
     * 批量删除药品信息
     * 
     * @param medIds 需要删除的药品信息主键集合
     * @return 结果
     */
    public int deleteMedInfoByMedIds(Long[] medIds);

    /**
     * 删除药品信息信息
     * 
     * @param medId 药品信息主键
     * @return 结果
     */
    public int deleteMedInfoByMedId(Long medId);
}
