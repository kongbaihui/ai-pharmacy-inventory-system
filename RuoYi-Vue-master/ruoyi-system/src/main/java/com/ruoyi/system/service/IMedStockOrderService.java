package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.MedStockBatch;
import com.ruoyi.system.domain.MedStockOrder;

/** 库存业务单Service接口 */
public interface IMedStockOrderService
{
    List<MedStockOrder> selectMedStockOrderList(MedStockOrder order);
    List<Map<String, Object>> selectMonthlyTrend();
    List<MedStockBatch> selectAvailableBatchOptions(Long medId, String orderType);
    MedStockOrder selectMedStockOrderById(Long orderId);
    int insertMedStockOrder(MedStockOrder order);
    int insertAndConfirmMedStockOrder(MedStockOrder order);
    int updateMedStockOrder(MedStockOrder order);
    int updateAndConfirmMedStockOrder(MedStockOrder order);
    int confirmMedStockOrder(Long orderId);
    int deleteMedStockOrderByIds(Long[] orderIds);
}
