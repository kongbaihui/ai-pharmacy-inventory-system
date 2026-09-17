package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MedStockCheck;
import com.ruoyi.system.domain.MedStockCheckItem;

/**
 * 库存盘点明细Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface MedStockCheckItemMapper 
{
    /**
     * 查询库存盘点明细
     * 
     * @param itemId 库存盘点明细主键
     * @return 库存盘点明细
     */
    public MedStockCheckItem selectMedStockCheckItemByItemId(Long itemId);

    /**
     * 查询库存盘点明细列表
     * 
     * @param medStockCheckItem 库存盘点明细
     * @return 库存盘点明细集合
     */
    public List<MedStockCheckItem> selectMedStockCheckItemList(MedStockCheckItem medStockCheckItem);

    /**
     * 按批次库存生成盘点明细的账面数据
     * 
     * @param medStockCheck 库存盘点
     * @return 盘点明细集合
     */
    public List<MedStockCheckItem> selectBookStockItemList(MedStockCheck medStockCheck);

    /**
     * 新增库存盘点明细
     * 
     * @param medStockCheckItem 库存盘点明细
     * @return 结果
     */
    public int insertMedStockCheckItem(MedStockCheckItem medStockCheckItem);

    /**
     * 修改库存盘点明细
     * 
     * @param medStockCheckItem 库存盘点明细
     * @return 结果
     */
    public int updateMedStockCheckItem(MedStockCheckItem medStockCheckItem);

    /**
     * 删除库存盘点明细
     * 
     * @param itemId 库存盘点明细主键
     * @return 结果
     */
    public int deleteMedStockCheckItemByItemId(Long itemId);

    /**
     * 批量删除库存盘点明细
     * 
     * @param itemIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedStockCheckItemByItemIds(Long[] itemIds);
}
