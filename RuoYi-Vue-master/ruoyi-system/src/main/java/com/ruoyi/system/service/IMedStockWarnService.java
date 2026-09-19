package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.MedStockWarn;

/**
 * 库存预警Service接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface IMedStockWarnService 
{
    /**
     * 查询库存预警
     * 
     * @param warnId 库存预警主键
     * @return 库存预警
     */
    public MedStockWarn selectMedStockWarnByWarnId(Long warnId);

    /**
     * 查询库存预警列表
     * 
     * @param medStockWarn 库存预警
     * @return 库存预警集合
     */
    public List<MedStockWarn> selectMedStockWarnList(MedStockWarn medStockWarn);

    /**
     * 扫描库存与效期数据，生成未处理的预警记录
     * 
     * @return 本次新增预警数量
     */
    public int scanStockWarn();

    /**
     * 新增库存预警
     * 
     * @param medStockWarn 库存预警
     * @return 结果
     */
    public int insertMedStockWarn(MedStockWarn medStockWarn);

    /**
     * 修改库存预警
     * 
     * @param medStockWarn 库存预警
     * @return 结果
     */
    public int updateMedStockWarn(MedStockWarn medStockWarn);

    /**
     * 处理预警（标记已处理或已忽略）
     * 
     * @param medStockWarn 库存预警
     * @return 结果
     */
    public int handleMedStockWarn(MedStockWarn medStockWarn);

    /**
     * 批量删除库存预警
     * 
     * @param warnIds 需要删除的库存预警主键集合
     * @return 结果
     */
    public int deleteMedStockWarnByWarnIds(Long[] warnIds);

    /**
     * 删除库存预警信息
     * 
     * @param warnId 库存预警主键
     * @return 结果
     */
    public int deleteMedStockWarnByWarnId(Long warnId);

    /**
     * 统计各类别预警数量（供库存预警与数据看板使用）
     * 
     * @return 统计数据
     */
    public Map<String, Object> selectWarnSummary();
}
