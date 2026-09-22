package com.ruoyi.system.service.impl;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.system.ai.tool.AiInventoryTools;
import com.ruoyi.system.domain.ai.AiInventoryMonthlyMetrics;
import com.ruoyi.system.domain.ai.AiOutboundRanking;
import com.ruoyi.system.mapper.AiInventoryMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiInventoryReportServiceImplTest
{
    private final AiInventoryMapper mapper = mock(AiInventoryMapper.class);

    @SuppressWarnings("unchecked")
    private final ObjectProvider<org.springframework.ai.chat.client.ChatClient.Builder> builderProvider =
            mock(ObjectProvider.class);

    @Test
    void shouldBuildDeterministicMetricsForCalendarMonth()
    {
        AiInventoryMonthlyMetrics aggregate = new AiInventoryMonthlyMetrics();
        aggregate.setInboundQty(120L);
        aggregate.setOutboundQty(75L);
        aggregate.setMovementCount(4L);
        AiOutboundRanking ranking = new AiOutboundRanking();
        ranking.setMedName("阿莫西林胶囊");
        when(mapper.selectMonthlyMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(aggregate);
        when(mapper.selectMonthlyTopOutbound(any(LocalDateTime.class), any(LocalDateTime.class), eq(5)))
                .thenReturn(List.of(ranking));
        when(mapper.selectExpiredCleanupCandidates(10)).thenReturn(List.of());
        when(mapper.selectDemandSnapshots(500)).thenReturn(List.of());

        AiInventoryReportServiceImpl service = new AiInventoryReportServiceImpl(
                builderProvider, mapper, new AiInventoryTools(mapper), new ObjectMapper());
        AiInventoryMonthlyMetrics result = service.getMonthlyMetrics(YearMonth.of(2026, 8));

        assertThat(result.getMonth()).isEqualTo("2026-08");
        assertThat(result.getPeriodStart()).isEqualTo(LocalDateTime.of(2026, 8, 1, 0, 0));
        assertThat(result.getPeriodEndExclusive()).isEqualTo(LocalDateTime.of(2026, 9, 1, 0, 0));
        assertThat(result.getInboundQty()).isEqualTo(120L);
        assertThat(result.getOutboundQty()).isEqualTo(75L);
        assertThat(result.getTopOutbound()).containsExactly(ranking);
        assertThat(result.isHasBusinessData()).isTrue();
        assertThat(result.getMetricBasis()).contains("flow_time");
        verify(mapper).selectMonthlyMetrics(LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0));
    }

    @Test
    void shouldMarkMonthWithoutMovementsAsNoBusinessData()
    {
        AiInventoryMonthlyMetrics aggregate = new AiInventoryMonthlyMetrics();
        aggregate.setMovementCount(0L);
        when(mapper.selectMonthlyMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(aggregate);
        when(mapper.selectMonthlyTopOutbound(any(LocalDateTime.class), any(LocalDateTime.class), eq(5)))
                .thenReturn(List.of());
        when(mapper.selectExpiredCleanupCandidates(10)).thenReturn(List.of());
        when(mapper.selectDemandSnapshots(500)).thenReturn(List.of());

        AiInventoryReportServiceImpl service = new AiInventoryReportServiceImpl(
                builderProvider, mapper, new AiInventoryTools(mapper), new ObjectMapper());
        AiInventoryMonthlyMetrics result = service.getMonthlyMetrics(YearMonth.of(2025, 1));

        assertThat(result.isHasBusinessData()).isFalse();
        assertThat(result.getTopOutbound()).isEmpty();
    }
}
