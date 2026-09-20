package com.ruoyi.system.ai.tool;

import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.ai.AiExpiryBatch;
import com.ruoyi.system.domain.ai.AiInventoryOverview;
import com.ruoyi.system.domain.ai.AiMedicineStock;
import com.ruoyi.system.mapper.AiInventoryMapper;

/**
 * 提供给 AI 的只读库存查询工具。
 */
@Component
public class AiInventoryTools
{
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;
    private static final int DEFAULT_EXPIRY_DAYS = 90;
    private static final int MAX_EXPIRY_DAYS = 365;

    private final AiInventoryMapper inventoryMapper;

    public AiInventoryTools(AiInventoryMapper inventoryMapper)
    {
        this.inventoryMapper = inventoryMapper;
    }

    @Tool(description = "按药品编码、通用名、商品名或批准文号查询当前库存；仅查询，不会修改库存")
    public List<AiMedicineStock> searchMedicineStock(
            @ToolParam(description = "查询关键词；为空时返回部分在用药品", required = false) String keyword,
            @ToolParam(description = "返回条数，范围1到20", required = false) Integer limit)
    {
        String safeKeyword = StringUtils.isBlank(keyword) ? null : keyword.trim();
        return inventoryMapper.searchMedicineStock(safeKeyword, normalizeLimit(limit));
    }

    @Tool(description = "查询当前库存数量低于库存下限的药品；仅查询，不会生成采购单")
    public List<AiMedicineStock> listLowStock(
            @ToolParam(description = "返回条数，范围1到20", required = false) Integer limit)
    {
        return inventoryMapper.selectLowStock(normalizeLimit(limit));
    }

    @Tool(description = "查询从今天起指定天数内到期且仍有剩余库存的批次，不包含已经过期的批次")
    public List<AiExpiryBatch> listExpiringBatches(
            @ToolParam(description = "未来天数，范围1到365，默认90", required = false) Integer days,
            @ToolParam(description = "返回条数，范围1到20", required = false) Integer limit)
    {
        int safeDays = days == null ? DEFAULT_EXPIRY_DAYS : Math.max(1, Math.min(days, MAX_EXPIRY_DAYS));
        return inventoryMapper.selectExpiringBatches(safeDays, normalizeLimit(limit));
    }

    @Tool(description = "获取在用药品数、库存总量、低库存数、积压数、临期批次数和过期批次数的实时总览")
    public AiInventoryOverview getInventoryOverview()
    {
        return inventoryMapper.selectInventoryOverview();
    }

    private int normalizeLimit(Integer limit)
    {
        return limit == null ? DEFAULT_LIMIT : Math.max(1, Math.min(limit, MAX_LIMIT));
    }
}
