package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.ai.AiExpiryBatch;
import com.ruoyi.system.domain.ai.AiDemandSnapshot;
import com.ruoyi.system.domain.ai.AiInventoryOverview;
import com.ruoyi.system.domain.ai.AiInventoryOperationsBrief;
import com.ruoyi.system.domain.ai.AiMedicineStock;
import com.ruoyi.system.domain.ai.AiOutboundRanking;

/**
 * AI 库存只读查询 Mapper。
 */
public interface AiInventoryMapper
{
    List<AiMedicineStock> searchMedicineStock(@Param("keyword") String keyword, @Param("limit") int limit);

    List<AiMedicineStock> selectLowStock(@Param("limit") int limit);

    List<AiExpiryBatch> selectExpiringBatches(@Param("days") int days, @Param("limit") int limit);

    AiInventoryOverview selectInventoryOverview();

    List<AiDemandSnapshot> selectDemandSnapshots(@Param("limit") int limit);

    AiInventoryOperationsBrief selectOperationsBrief(@Param("days") int days);

    List<AiOutboundRanking> selectTopOutbound(@Param("days") int days, @Param("limit") int limit);
}
