package com.ruoyi.system.mapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class MedInventoryMapperXmlTest
{
    @Test
    void shouldParseInventoryConcurrencyAndTrendStatements() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.getTypeAliasRegistry().registerAliases("com.ruoyi.system.domain");

        parse(configuration, "mapper/system/MedStockOrderMapper.xml");
        parse(configuration, "mapper/system/MedStockCheckMapper.xml");
        parse(configuration, "mapper/system/MedStockBatchMapper.xml");
        parse(configuration, "mapper/system/MedExpiredCleanMapper.xml");

        assertThat(configuration.getMappedStatementNames()).contains(
                "com.ruoyi.system.mapper.MedStockOrderMapper.selectMonthlyTrend",
                "com.ruoyi.system.mapper.MedStockCheckMapper.selectMedStockCheckForUpdate",
                "com.ruoyi.system.mapper.MedStockBatchMapper.selectMedStockBatchForUpdate",
                "com.ruoyi.system.mapper.MedExpiredCleanMapper.selectMedExpiredCleanForUpdate");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("batchId", 1L);
        parameters.put("changeQty", -1L);
        BoundSql boundSql = configuration
                .getMappedStatement("com.ruoyi.system.mapper.MedStockBatchMapper.updateBatchRemainQty")
                .getBoundSql(parameters);
        String normalizedSql = boundSql.getSql().replaceAll("\\s+", " ").trim().toLowerCase();

        assertThat(normalizedSql)
                .contains("ifnull(remain_qty, 0) + ? >= 0")
                .doesNotContain("greatest(");
    }

    private void parse(Configuration configuration, String resourceName) throws Exception
    {
        try (InputStream input = new ClassPathResource(resourceName).getInputStream())
        {
            new XMLMapperBuilder(input, configuration, resourceName, configuration.getSqlFragments()).parse();
        }
    }
}
