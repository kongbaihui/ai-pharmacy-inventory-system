package com.ruoyi.system.mapper;

import java.io.InputStream;
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
                        "com.ruoyi.system.mapper.AiInventoryMapper.selectInventoryOverview");
    }
}
