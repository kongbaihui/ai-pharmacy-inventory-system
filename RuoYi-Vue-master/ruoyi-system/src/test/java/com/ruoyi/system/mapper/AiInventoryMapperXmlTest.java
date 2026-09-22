package com.ruoyi.system.mapper;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class AiInventoryMapperXmlTest
{
    @Test
    void shouldParseAllReadOnlyStatements() throws Exception
    {
        Configuration configuration = new Configuration();
        String resourceName = "mapper/system/AiInventoryMapper.xml";
        try (InputStream input = new ClassPathResource(resourceName).getInputStream())
        {
            new XMLMapperBuilder(input, configuration, resourceName, configuration.getSqlFragments()).parse();
        }

        assertThat(configuration.getMappedStatementNames())
                .contains("com.ruoyi.system.mapper.AiInventoryMapper.searchMedicineStock",
                        "com.ruoyi.system.mapper.AiInventoryMapper.selectLowStock",
                        "com.ruoyi.system.mapper.AiInventoryMapper.selectExpiringBatches",
                        "com.ruoyi.system.mapper.AiInventoryMapper.selectExpiredCleanupCandidates",
                        "com.ruoyi.system.mapper.AiInventoryMapper.selectInventoryOverview",
                        "com.ruoyi.system.mapper.AiInventoryMapper.selectMonthlyMetrics",
                        "com.ruoyi.system.mapper.AiInventoryMapper.selectMonthlyTopOutbound");

        assertMappedProperties(configuration, "selectExpiringBatches",
                "medId", "batchNo", "expireDate", "remainDays", "remainQty", "supplierName");
        assertMappedProperties(configuration, "selectExpiredCleanupCandidates",
                "medId", "batchId", "expiredDays", "hasPendingClean", "pendingCleanId", "suggestedAction");
        assertMappedProperties(configuration, "selectInventoryOverview",
                "medicineCount", "stockQty", "lowStockCount", "overstockCount",
                "expiringBatchCount", "expiredBatchCount");
        assertMappedProperties(configuration, "selectDemandSnapshots",
                "medId", "currentQty", "availableQty", "stockMin", "stockMax", "outboundQty30d");
        assertMappedProperties(configuration, "selectOperationsBrief",
                "inboundQty", "outboundQty", "returnQty", "adjustmentNetQty", "expiredCleanQty",
                "movementCount", "lowStockCount", "expiringBatchCount", "expiredBatchCount");
        assertMappedProperties(configuration, "selectTopOutbound",
                "medId", "medName", "outboundQty", "outboundTransactions");
        assertMappedProperties(configuration, "selectMonthlyMetrics",
                "inboundQty", "outboundQty", "returnQty", "adjustmentNetQty", "expiredCleanQty",
                "movementCount", "lowStockCount", "expiringBatchCount", "expiredBatchCount");
        assertMappedProperties(configuration, "selectMonthlyTopOutbound",
                "medId", "medName", "outboundQty", "outboundTransactions");
    }

    private void assertMappedProperties(Configuration configuration, String statement, String... properties)
    {
        String statementId = "com.ruoyi.system.mapper.AiInventoryMapper." + statement;
        Set<String> mappedProperties = configuration.getMappedStatement(statementId).getResultMaps().get(0)
                .getResultMappings().stream()
                .map(mapping -> mapping.getProperty())
                .collect(Collectors.toSet());
        assertThat(mappedProperties).contains(properties);
    }
}
