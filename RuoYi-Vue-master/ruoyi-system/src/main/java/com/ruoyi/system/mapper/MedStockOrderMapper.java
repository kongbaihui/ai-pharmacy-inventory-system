package com.ruoyi.system.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MedStockBatch;
import com.ruoyi.system.domain.MedStockOrder;
import com.ruoyi.system.domain.MedStockOrderItem;

/** 库存业务单Mapper接口 */
public interface MedStockOrderMapper
{
    List<MedStockOrder> selectMedStockOrderList(MedStockOrder order);
    MedStockOrder selectMedStockOrderById(Long orderId);
    MedStockOrder selectMedStockOrderForUpdate(Long orderId);
    List<MedStockOrderItem> selectItemListByOrderId(Long orderId);
    int insertMedStockOrder(MedStockOrder order);
    int updateMedStockOrder(MedStockOrder order);
    int updateOrderConfirmed(MedStockOrder order);
    int deleteItemsByOrderId(Long orderId);
    int insertMedStockOrderItem(MedStockOrderItem item);
    int updateItemBatchId(@Param("itemId") Long itemId, @Param("batchId") Long batchId);
    int deleteMedStockOrderById(Long orderId);
    MedStockBatch selectBatchForUpdate(Long batchId);
    MedStockBatch selectBatchByMedAndNoForUpdate(@Param("medId") Long medId, @Param("batchNo") String batchNo);
    int increaseBatchByInbound(@Param("batchId") Long batchId, @Param("quantity") Long quantity,
            @Param("unitPrice") BigDecimal unitPrice, @Param("updateBy") String updateBy);
    int decreaseBatchForOutbound(@Param("batchId") Long batchId, @Param("quantity") Long quantity,
            @Param("updateBy") String updateBy);
    int increaseBatchForReturn(@Param("batchId") Long batchId, @Param("quantity") Long quantity,
            @Param("updateBy") String updateBy);
    int countEnabledMed(Long medId);
    int countEnabledSupplier(Long supplierId);
}
