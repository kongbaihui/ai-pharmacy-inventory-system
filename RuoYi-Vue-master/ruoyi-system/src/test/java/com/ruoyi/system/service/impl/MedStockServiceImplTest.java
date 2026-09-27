package com.ruoyi.system.service.impl;

import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.MedStock;
import com.ruoyi.system.domain.MedStockFlow;
import com.ruoyi.system.mapper.MedStockMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MedStockServiceImplTest
{
    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUpdateTotalStockAndWriteMatchingFlow()
    {
        MedStockMapper mapper = mock(MedStockMapper.class);
        MedStockServiceImpl service = service(mapper);
        MedStock stock = new MedStock();
        stock.setMedId(6L);
        stock.setTotalQty(10L);

        when(mapper.selectMedStockByMedIdForUpdate(6L)).thenReturn(stock);
        when(mapper.updateMedStockQty(stock)).thenReturn(1);
        when(mapper.insertMedStockFlow(any(MedStockFlow.class))).thenAnswer(invocation -> {
            MedStockFlow flow = invocation.getArgument(0);
            assertThat(flow.getBeforeQty()).isEqualTo(10L);
            assertThat(flow.getAfterQty()).isEqualTo(14L);
            assertThat(flow.getChangeQty()).isEqualTo(4L);
            assertThat(flow.getBizNo()).isEqualTo("RK-6");
            return 1;
        });

        assertThat(service.adjustStock(6L, 61L, 4L, "1", "RK-6", 600L, "入库")).isEqualTo(1);
        assertThat(stock.getTotalQty()).isEqualTo(14L);
    }

    @Test
    void shouldRejectNegativeTotalStockWithoutWritingFlow()
    {
        MedStockMapper mapper = mock(MedStockMapper.class);
        MedStockServiceImpl service = service(mapper);
        MedStock stock = new MedStock();
        stock.setMedId(7L);
        stock.setTotalQty(2L);
        when(mapper.selectMedStockByMedIdForUpdate(7L)).thenReturn(stock);

        assertThatThrownBy(() -> service.adjustStock(7L, 71L, -3L, "2", "CK-7", 700L, "出库"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("库存数量不足");
        verify(mapper, never()).updateMedStockQty(any(MedStock.class));
        verify(mapper, never()).insertMedStockFlow(any(MedStockFlow.class));
    }

    @Test
    void shouldFailWhenFlowWasNotInserted()
    {
        MedStockMapper mapper = mock(MedStockMapper.class);
        MedStockServiceImpl service = service(mapper);
        MedStock stock = new MedStock();
        stock.setMedId(8L);
        stock.setTotalQty(5L);
        when(mapper.selectMedStockByMedIdForUpdate(8L)).thenReturn(stock);
        when(mapper.updateMedStockQty(stock)).thenReturn(1);
        when(mapper.insertMedStockFlow(any(MedStockFlow.class))).thenReturn(0);

        assertThatThrownBy(() -> service.adjustStock(8L, 81L, 1L, "3", "TK-8", 800L, "退库"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("流水生成失败");
    }

    private MedStockServiceImpl service(MedStockMapper mapper)
    {
        SysUser user = new SysUser();
        user.setUserName("tester");
        LoginUser loginUser = new LoginUser(user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
        MedStockServiceImpl service = new MedStockServiceImpl();
        ReflectionTestUtils.setField(service, "medStockMapper", mapper);
        return service;
    }
}
