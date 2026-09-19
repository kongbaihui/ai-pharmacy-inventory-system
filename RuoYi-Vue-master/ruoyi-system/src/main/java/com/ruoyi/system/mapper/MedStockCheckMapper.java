package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MedStockCheck;
import com.ruoyi.system.domain.MedStockCheckItem;

/**
 * 库存盘点Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface MedStockCheckMapper 
{
    /**
     * 查询库存盘点
     * 
     * @param checkId 库存盘点主键
     * @return 库存盘点
     */
    public MedStockCheck selectMedStockCheckByCheckId(Long checkId);

    /**
     * 查询库存盘点列表
     * 
     * @param medStockCheck 库存盘点
     * @return 库存盘点集合
     */
    public List<MedStockCheck> selectMedStockCheckList(MedStockCheck medStockCheck);

    /**
     * 新增库存盘点
     * 
     * @param medStockCheck 库存盘点
     * @return 结果
     */
    public int insertMedStockCheck(MedStockCheck medStockCheck);

    /**
     * 修改库存盘点
     * 
     * @param medStockCheck 库存盘点
     * @return 结果
     */
    public int updateMedStockCheck(MedStockCheck medStockCheck);

    /**
     * 删除库存盘点
     * 
     * @param checkId 库存盘点主键
     * @return 结果
     */
    public int deleteMedStockCheckByCheckId(Long checkId);

    /**
     * 批量删除库存盘点
     * 
     * @param checkIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedStockCheckByCheckIds(Long[] checkIds);

    /**
     * 根据盘点ID删除盘点明细信息
     * 
     * @param checkId 库存盘点主键
     * @return 结果
     */
    public int deleteMedStockCheckItemByCheckId(Long checkId);

    /**
     * 批量删除盘点明细信息
     * 
     * @param checkIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedStockCheckItemByCheckIds(Long[] checkIds);

    /**
     * 批量新增盘点明细信息
     * 
     * @param medStockCheckItemList 盘点明细集合
     * @return 结果
     */
    public int batchMedStockCheckItem(List<MedStockCheckItem> medStockCheckItemList);

    /**
     * 查询当日前缀下最大的盘点单号
     * 
     * @param prefix 单号前缀
     * @return 盘点单号
     */
    public String selectMaxCheckNo(String prefix);
}
