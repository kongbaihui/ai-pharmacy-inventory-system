package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedStockOrder;

/** 库存业务单Service接口 */
public interface IMedStockOrderService
{
    List<MedStockOrder> selectMedStockOrderList(MedStockOrder order);
    MedStockOrder selectMedStockOrderById(Long orderId);
    int insertMedStockOrder(MedStockOrder order);
    int updateMedStockOrder(MedStockOrder order);
    int confirmMedStockOrder(Long orderId);
    int deleteMedStockOrderByIds(Long[] orderIds);
}
