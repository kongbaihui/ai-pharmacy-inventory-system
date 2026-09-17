package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MedStockWarn;

/**
 * 库存预警Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface MedStockWarnMapper 
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
     * 查询库存预警来源数据（库存不足、库存积压、近效期、已过期）
     * 
     * @return 预警数据集合
     */
    public List<MedStockWarn> selectStockWarnSourceList();

    /**
     * 统计同一药品、批次、类型的未处理预警数量
     * 
     * @param medId 药品ID
     * @param batchId 批次ID
     * @param warnType 预警类型
     * @return 数量
     */
    public int countUnhandleWarn(@Param("medId") Long medId, @Param("batchId") Long batchId, @Param("warnType") String warnType);

    /**
     * 新增库存预警
     * 
     * @param medStockWarn 库存预警
     * @return 结果
     */
    public int insertMedStockWarn(MedStockWarn medStockWarn);

    /**
     * 批量新增库存预警
     * 
     * @param medStockWarnList 库存预警集合
     * @return 结果
     */
    public int batchMedStockWarn(List<MedStockWarn> medStockWarnList);

    /**
     * 修改库存预警
     * 
     * @param medStockWarn 库存预警
     * @return 结果
     */
    public int updateMedStockWarn(MedStockWarn medStockWarn);

    /**
     * 批量修改预警处理状态
     * 
     * @param medStockWarn 库存预警
     * @return 结果
     */
    public int batchUpdateHandleStatus(MedStockWarn medStockWarn);

    /**
     * 删除库存预警
     * 
     * @param warnId 库存预警主键
     * @return 结果
     */
    public int deleteMedStockWarnByWarnId(Long warnId);

    /**
     * 批量删除库存预警
     * 
     * @param warnIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedStockWarnByWarnIds(Long[] warnIds);

    /**
     * 统计各类别预警数量（供库存预警与数据看板使用）
     * 
     * @return 统计数据
     */
    public Map<String, Object> selectWarnSummary();
}
