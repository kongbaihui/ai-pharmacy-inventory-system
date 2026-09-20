package com.ruoyi.system.ai.tool;

import org.junit.jupiter.api.Test;
import com.ruoyi.system.mapper.AiInventoryMapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AiInventoryToolsTest
{
    private final AiInventoryMapper mapper = mock(AiInventoryMapper.class);
    private final AiInventoryTools tools = new AiInventoryTools(mapper);

    @Test
    void shouldTrimKeywordAndClampResultLimit()
    {
        tools.searchMedicineStock("  阿莫西林  ", 100);

        verify(mapper).searchMedicineStock("阿莫西林", 20);
    }

    @Test
    void shouldUseSafeDefaultsForOptionalArguments()
    {
        tools.searchMedicineStock(" ", null);
        tools.listExpiringBatches(null, null);

        verify(mapper).searchMedicineStock(null, 10);
        verify(mapper).selectExpiringBatches(90, 10);
    }

    @Test
    void shouldClampExpiryWindow()
    {
        tools.listExpiringBatches(9999, 0);

        verify(mapper).selectExpiringBatches(365, 1);
    }
}
