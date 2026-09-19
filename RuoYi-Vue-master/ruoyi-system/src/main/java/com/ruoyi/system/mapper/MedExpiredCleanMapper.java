package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MedExpiredClean;

/**
 * 过期药品清理Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface MedExpiredCleanMapper 
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
     * 删除过期药品清理
     * 
     * @param cleanId 过期药品清理主键
     * @return 结果
     */
    public int deleteMedExpiredCleanByCleanId(Long cleanId);

    /**
     * 批量删除过期药品清理
     * 
     * @param cleanIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedExpiredCleanByCleanIds(Long[] cleanIds);

    /**
     * 查询当日前缀下最大的清理单号
     * 
     * @param prefix 单号前缀
     * @return 清理单号
     */
    public String selectMaxCleanNo(String prefix);
}
