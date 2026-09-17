package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedExpiredClean;

/**
 * 过期药品清理Service接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface IMedExpiredCleanService 
{
    /**
     * 查询过期药品清理
     * 
     * @param cleanId 过期药品清理主键
     * @return 过期药品清理
     */
    public MedExpiredClean selectMedExpiredCleanByCleanId(Long cleanId);

    /**
     * 查询过期药品清理列表
     * 
     * @param medExpiredClean 过期药品清理
     * @return 过期药品清理集合
     */
    public List<MedExpiredClean> selectMedExpiredCleanList(MedExpiredClean medExpiredClean);

    /**
     * 新增过期药品清理
     * 
     * @param medExpiredClean 过期药品清理
     * @return 结果
     */
    public int insertMedExpiredClean(MedExpiredClean medExpiredClean);

    /**
     * 修改过期药品清理
     * 
     * @param medExpiredClean 过期药品清理
     * @return 结果
     */
    public int updateMedExpiredClean(MedExpiredClean medExpiredClean);

    /**
     * 批量删除过期药品清理
     * 
     * @param cleanIds 需要删除的过期药品清理主键集合
     * @return 结果
     */
    public int deleteMedExpiredCleanByCleanIds(Long[] cleanIds);

    /**
     * 删除过期药品清理信息
     * 
     * @param cleanId 过期药品清理主键
     * @return 结果
     */
    public int deleteMedExpiredCleanByCleanId(Long cleanId);

    /**
     * 确认清理：扣减库存与批次数量，并登记库存流水
     * 
     * @param cleanId 过期药品清理主键
     * @param remark 确认说明
     * @return 结果
     */
    public int confirmMedExpiredClean(Long cleanId, String remark);

    /**
     * 驳回清理申请
     * 
     * @param cleanId 过期药品清理主键
     * @param remark 驳回说明
     * @return 结果
     */
    public int rejectMedExpiredClean(Long cleanId, String remark);
}
