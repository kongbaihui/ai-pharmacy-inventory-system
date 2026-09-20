package com.ruoyi.system.ai.tool;

import java.util.List;
import org.junit.jupiter.api.Test;
import com.ruoyi.system.domain.ai.AiDemandSnapshot;
import com.ruoyi.system.domain.ai.AiInventoryOperationsBrief;
import com.ruoyi.system.domain.ai.AiOutboundRanking;
import com.ruoyi.system.domain.ai.AiReplenishmentAdvice;
import com.ruoyi.system.mapper.AiInventoryMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void shouldFallBackToMinimumStockWithoutRecentOutbound()
    {
        AiDemandSnapshot snapshot = snapshot(1L, "布洛芬", 25, 20, 60, 200, 0);

        AiReplenishmentAdvice advice = tools.calculateAdvice(snapshot, 30);

        assertThat(advice.getTargetStock()).isEqualTo(60);
        assertThat(advice.getSuggestedOrderQty()).isEqualTo(40);
        assertThat(advice.getAverageDailyOutbound()).isEqualByComparingTo("0.00");
        assertThat(advice.getDaysOfSupply()).isNull();
        assertThat(advice.getUrgency()).isEqualTo("高");
        assertThat(advice.getReason()).contains("库存下限");
    }

    @Test
    void shouldCapDemandTargetAtConfiguredMaximum()
    {
        AiDemandSnapshot snapshot = snapshot(2L, "阿莫西林", 10, 5, 20, 100, 300);

        AiReplenishmentAdvice advice = tools.calculateAdvice(snapshot, 30);

        assertThat(advice.getAverageDailyOutbound()).isEqualByComparingTo("10.00");
        assertThat(advice.getDaysOfSupply()).isEqualByComparingTo("0.5");
        assertThat(advice.getTargetStock()).isEqualTo(100);
        assertThat(advice.getSuggestedOrderQty()).isEqualTo(95);
        assertThat(advice.getUrgency()).isEqualTo("紧急");
        assertThat(advice.getReason()).contains("库存上限");
    }

    @Test
    void shouldReturnOnlyPositiveAdviceInUrgencyOrder()
    {
        AiDemandSnapshot routine = snapshot(3L, "常规药", 10, 10, 0, 100, 30);
        AiDemandSnapshot urgent = snapshot(2L, "紧急药", 0, 0, 20, 100, 60);
        AiDemandSnapshot sufficient = snapshot(1L, "充足药", 100, 100, 20, 100, 0);
        when(mapper.selectDemandSnapshots(500)).thenReturn(List.of(routine, sufficient, urgent));

        List<AiReplenishmentAdvice> results = tools.listReplenishmentAdvice(30, 10);

        assertThat(results).extracting(AiReplenishmentAdvice::getMedName)
                .containsExactly("紧急药", "常规药");
        verify(mapper).selectDemandSnapshots(500);
    }

    @Test
    void shouldClampOperationsBriefWindowAndRankingLimit()
    {
        AiInventoryOperationsBrief brief = new AiInventoryOperationsBrief();
        AiOutboundRanking ranking = new AiOutboundRanking();
        ranking.setMedName("阿莫西林胶囊");
        when(mapper.selectOperationsBrief(90)).thenReturn(brief);
        when(mapper.selectTopOutbound(90, 10)).thenReturn(List.of(ranking));

        AiInventoryOperationsBrief result = tools.getInventoryOperationsBrief(365, 100);

        assertThat(result.getAnalysisDays()).isEqualTo(90);
        assertThat(result.getTopOutbound()).containsExactly(ranking);
        verify(mapper).selectOperationsBrief(90);
        verify(mapper).selectTopOutbound(90, 10);
    }

    private AiDemandSnapshot snapshot(Long id, String name, int currentQty, int availableQty,
            int stockMin, int stockMax, int outboundQty)
    {
        AiDemandSnapshot snapshot = new AiDemandSnapshot();
        snapshot.setMedId(id);
        snapshot.setMedCode("M" + id);
        snapshot.setMedName(name);
        snapshot.setMedSpec("测试规格");
        snapshot.setUnit("盒");
        snapshot.setCurrentQty(currentQty);
        snapshot.setAvailableQty(availableQty);
        snapshot.setStockMin(stockMin);
        snapshot.setStockMax(stockMax);
        snapshot.setOutboundQty30d(outboundQty);
        return snapshot;
    }
}
