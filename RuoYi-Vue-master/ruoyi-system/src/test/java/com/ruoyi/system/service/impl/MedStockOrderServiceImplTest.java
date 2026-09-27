package com.ruoyi.system.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.MedStockBatch;
import com.ruoyi.system.domain.MedStockOrder;
import com.ruoyi.system.domain.MedStockOrderItem;
import com.ruoyi.system.mapper.MedStockBatchMapper;
import com.ruoyi.system.mapper.MedStockOrderMapper;
import com.ruoyi.system.service.IMedStockService;
import com.ruoyi.system.service.IMedStockWarnService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class MedStockOrderServiceImplTest
{
    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldInsertAndConfirmInOneTransactionalOperation() throws Exception
    {
        MedStockOrderServiceImpl service = spy(new MedStockOrderServiceImpl());
        MedStockOrder order = new MedStockOrder();
        doAnswer(invocation -> {
            order.setOrderId(21L);
            return 1;
        }).when(service).insertMedStockOrder(order);
        doReturn(1).when(service).confirmMedStockOrder(21L);

        assertThat(service.insertAndConfirmMedStockOrder(order)).isEqualTo(1);
        verify(service).insertMedStockOrder(order);
        verify(service).confirmMedStockOrder(21L);
        assertThat(MedStockOrderServiceImpl.class
                .getMethod("insertAndConfirmMedStockOrder", MedStockOrder.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void shouldUpdateAndConfirmExistingDraftInOneTransactionalOperation() throws Exception
    {
        MedStockOrderServiceImpl service = spy(new MedStockOrderServiceImpl());
        MedStockOrder order = new MedStockOrder();
        order.setOrderId(22L);
        doReturn(1).when(service).updateMedStockOrder(order);
        doReturn(1).when(service).confirmMedStockOrder(22L);

        assertThat(service.updateAndConfirmMedStockOrder(order)).isEqualTo(1);
        verify(service).updateMedStockOrder(order);
        verify(service).confirmMedStockOrder(22L);
        assertThat(MedStockOrderServiceImpl.class
                .getMethod("updateAndConfirmMedStockOrder", MedStockOrder.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void shouldNotConfirmWhenSavingDraftFails()
    {
        MedStockOrderServiceImpl service = spy(new MedStockOrderServiceImpl());
        MedStockOrder order = new MedStockOrder();
        doReturn(0).when(service).insertMedStockOrder(order);

        assertThatThrownBy(() -> service.insertAndConfirmMedStockOrder(order))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("未更新库存");
        verify(service, never()).confirmMedStockOrder(anyLong());
    }

    @Test
    void shouldCreateBatchAdjustStockAndConfirmInbound()
    {
        TestContext context = createContext();
        MedStockOrder order = draftOrder(31L, "RK-31", "1");
        order.setSupplierId(9L);
        MedStockOrderItem item = item(311L, 3L, 8L);
        item.setBatchNo("BATCH-31");
        item.setExpireDate(new Date(System.currentTimeMillis() + 86400000L));

        when(context.orderMapper.selectMedStockOrderForUpdate(31L)).thenReturn(order);
        when(context.orderMapper.selectItemListByOrderId(31L)).thenReturn(List.of(item));
        when(context.orderMapper.countEnabledSupplier(9L)).thenReturn(1);
        when(context.orderMapper.countEnabledMed(3L)).thenReturn(1);
        when(context.orderMapper.selectBatchByMedAndNoForUpdate(3L, "BATCH-31")).thenReturn(null);
        doAnswer(invocation -> {
            MedStockBatch batch = invocation.getArgument(0);
            batch.setBatchId(41L);
            return 1;
        }).when(context.batchMapper).insertMedStockBatch(org.mockito.ArgumentMatchers.any(MedStockBatch.class));
        when(context.orderMapper.updateItemBatchId(311L, 41L)).thenReturn(1);
        when(context.stockService.adjustStock(3L, 41L, 8L, "1", "RK-31", 31L, "药品入库")).thenReturn(1);
        when(context.orderMapper.updateOrderConfirmed(order)).thenReturn(1);

        assertThat(context.service.confirmMedStockOrder(31L)).isEqualTo(1);
        assertThat(item.getBatchId()).isEqualTo(41L);
        verify(context.batchMapper).refreshBatchStatus();
        verify(context.stockWarnService).scanStockWarn();
    }

    @Test
    void shouldRejectOutboundWhenBatchStockIsInsufficientBeforeChangingTotalStock()
    {
        TestContext context = createContext();
        MedStockOrder order = draftOrder(32L, "CK-32", "2");
        order.setDepartment("内科");
        MedStockOrderItem item = item(321L, 4L, 6L);
        item.setBatchId(42L);
        MedStockBatch batch = batch(42L, 4L, 2L, 10L);

        when(context.orderMapper.selectMedStockOrderForUpdate(32L)).thenReturn(order);
        when(context.orderMapper.selectItemListByOrderId(32L)).thenReturn(List.of(item));
        when(context.orderMapper.countEnabledMed(4L)).thenReturn(1);
        when(context.orderMapper.selectBatchForUpdate(42L)).thenReturn(batch);
        when(context.orderMapper.decreaseBatchForOutbound(42L, 6L, "tester")).thenReturn(0);

        assertThatThrownBy(() -> context.service.confirmMedStockOrder(32L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("批次库存不足");
        verify(context.stockService, never()).adjustStock(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString());
        verify(context.orderMapper, never()).updateOrderConfirmed(order);
    }

    @Test
    void shouldDecreaseBatchAndTotalStockWhenConfirmingOutbound()
    {
        TestContext context = createContext();
        MedStockOrder order = draftOrder(36L, "CK-36", "2");
        order.setDepartment("内科");
        MedStockOrderItem item = item(361L, 4L, 4L);
        item.setBatchId(46L);
        MedStockBatch batch = batch(46L, 4L, 10L, 10L);

        when(context.orderMapper.selectMedStockOrderForUpdate(36L)).thenReturn(order);
        when(context.orderMapper.selectItemListByOrderId(36L)).thenReturn(List.of(item));
        when(context.orderMapper.countEnabledMed(4L)).thenReturn(1);
        when(context.orderMapper.selectBatchForUpdate(46L)).thenReturn(batch);
        when(context.orderMapper.decreaseBatchForOutbound(46L, 4L, "tester")).thenReturn(1);
        when(context.stockService.adjustStock(4L, 46L, -4L, "2", "CK-36", 36L, "药品出库")).thenReturn(1);
        when(context.orderMapper.updateOrderConfirmed(order)).thenReturn(1);

        assertThat(context.service.confirmMedStockOrder(36L)).isEqualTo(1);
        verify(context.orderMapper).decreaseBatchForOutbound(46L, 4L, "tester");
        verify(context.stockService).adjustStock(4L, 46L, -4L, "2", "CK-36", 36L, "药品出库");
    }

    @Test
    void shouldRejectReturnThatExceedsCumulativeInboundQuantity()
    {
        TestContext context = createContext();
        MedStockOrder order = draftOrder(33L, "TK-33", "3");
        order.setDepartment("外科");
        MedStockOrderItem item = item(331L, 5L, 3L);
        item.setBatchId(43L);
        MedStockBatch batch = batch(43L, 5L, 9L, 10L);

        when(context.orderMapper.selectMedStockOrderForUpdate(33L)).thenReturn(order);
        when(context.orderMapper.selectItemListByOrderId(33L)).thenReturn(List.of(item));
        when(context.orderMapper.countEnabledMed(5L)).thenReturn(1);
        when(context.orderMapper.selectBatchForUpdate(43L)).thenReturn(batch);
        when(context.orderMapper.increaseBatchForReturn(43L, 3L, "tester")).thenReturn(0);

        assertThatThrownBy(() -> context.service.confirmMedStockOrder(33L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("超过该批次累计入库数量");
        verify(context.stockService, never()).adjustStock(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldIncreaseBatchAndTotalStockWhenConfirmingReturn()
    {
        TestContext context = createContext();
        MedStockOrder order = draftOrder(37L, "TK-37", "3");
        order.setDepartment("外科");
        MedStockOrderItem item = item(371L, 5L, 3L);
        item.setBatchId(47L);
        MedStockBatch batch = batch(47L, 5L, 4L, 10L);

        when(context.orderMapper.selectMedStockOrderForUpdate(37L)).thenReturn(order);
        when(context.orderMapper.selectItemListByOrderId(37L)).thenReturn(List.of(item));
        when(context.orderMapper.countEnabledMed(5L)).thenReturn(1);
        when(context.orderMapper.selectBatchForUpdate(47L)).thenReturn(batch);
        when(context.orderMapper.increaseBatchForReturn(47L, 3L, "tester")).thenReturn(1);
        when(context.stockService.adjustStock(5L, 47L, 3L, "3", "TK-37", 37L, "科室药品退库")).thenReturn(1);
        when(context.orderMapper.updateOrderConfirmed(order)).thenReturn(1);

        assertThat(context.service.confirmMedStockOrder(37L)).isEqualTo(1);
        verify(context.orderMapper).increaseBatchForReturn(47L, 3L, "tester");
        verify(context.stockService).adjustStock(5L, 47L, 3L, "3", "TK-37", 37L, "科室药品退库");
    }

    @Test
    void shouldRejectExpiredBatchBeforeChangingStock()
    {
        TestContext context = createContext();
        MedStockOrder order = draftOrder(38L, "CK-38", "2");
        order.setDepartment("急诊科");
        MedStockOrderItem item = item(381L, 6L, 1L);
        item.setBatchId(48L);
        MedStockBatch batch = batch(48L, 6L, 5L, 10L);
        batch.setExpireDate(new Date(System.currentTimeMillis() - 86400000L));

        when(context.orderMapper.selectMedStockOrderForUpdate(38L)).thenReturn(order);
        when(context.orderMapper.selectItemListByOrderId(38L)).thenReturn(List.of(item));
        when(context.orderMapper.countEnabledMed(6L)).thenReturn(1);
        when(context.orderMapper.selectBatchForUpdate(48L)).thenReturn(batch);

        assertThatThrownBy(() -> context.service.confirmMedStockOrder(38L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已过期");
        verify(context.orderMapper, never()).decreaseBatchForOutbound(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString());
        verify(context.stockService, never()).adjustStock(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRejectRepeatedConfirmation()
    {
        TestContext context = createContext();
        MedStockOrder order = draftOrder(34L, "RK-34", "1");
        order.setOrderStatus("1");
        when(context.orderMapper.selectMedStockOrderForUpdate(34L)).thenReturn(order);

        assertThatThrownBy(() -> context.service.confirmMedStockOrder(34L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已确认");
        verify(context.orderMapper, never()).selectItemListByOrderId(34L);
    }

    @Test
    void shouldNotReplaceDraftItemsWhenHeaderUpdateFails()
    {
        TestContext context = createContext();
        MedStockOrder saved = draftOrder(35L, "RK-35", "1");
        saved.setSupplierId(9L);
        MedStockOrder changed = draftOrder(35L, "RK-35", "1");
        changed.setSupplierId(9L);
        MedStockOrderItem item = item(null, 3L, 1L);
        item.setBatchNo("BATCH-35");
        item.setExpireDate(new Date(System.currentTimeMillis() + 86400000L));
        changed.setItemList(List.of(item));

        when(context.orderMapper.selectMedStockOrderForUpdate(35L)).thenReturn(saved);
        when(context.orderMapper.countEnabledSupplier(9L)).thenReturn(1);
        when(context.orderMapper.countEnabledMed(3L)).thenReturn(1);
        when(context.orderMapper.updateMedStockOrder(changed)).thenReturn(0);

        assertThatThrownBy(() -> context.service.updateMedStockOrder(changed))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("修改失败");
        verify(context.orderMapper, never()).deleteItemsByOrderId(35L);
    }

    private TestContext createContext()
    {
        SysUser user = new SysUser();
        user.setUserName("tester");
        LoginUser loginUser = new LoginUser(user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

        TestContext context = new TestContext();
        context.service = new MedStockOrderServiceImpl();
        context.orderMapper = mock(MedStockOrderMapper.class);
        context.batchMapper = mock(MedStockBatchMapper.class);
        context.stockService = mock(IMedStockService.class);
        context.stockWarnService = mock(IMedStockWarnService.class);
        ReflectionTestUtils.setField(context.service, "orderMapper", context.orderMapper);
        ReflectionTestUtils.setField(context.service, "batchMapper", context.batchMapper);
        ReflectionTestUtils.setField(context.service, "stockService", context.stockService);
        ReflectionTestUtils.setField(context.service, "stockWarnService", context.stockWarnService);
        return context;
    }

    private MedStockOrder draftOrder(Long orderId, String orderNo, String orderType)
    {
        MedStockOrder order = new MedStockOrder();
        order.setOrderId(orderId);
        order.setOrderNo(orderNo);
        order.setOrderType(orderType);
        order.setOrderStatus("0");
        return order;
    }

    private MedStockOrderItem item(Long itemId, Long medId, Long quantity)
    {
        MedStockOrderItem item = new MedStockOrderItem();
        item.setItemId(itemId);
        item.setMedId(medId);
        item.setQuantity(quantity);
        return item;
    }

    private MedStockBatch batch(Long batchId, Long medId, Long remainQty, Long batchQty)
    {
        MedStockBatch batch = new MedStockBatch();
        batch.setBatchId(batchId);
        batch.setMedId(medId);
        batch.setRemainQty(remainQty);
        batch.setBatchQty(batchQty);
        batch.setExpireDate(new Date(System.currentTimeMillis() + 86400000L));
        return batch;
    }

    private static class TestContext
    {
        private MedStockOrderServiceImpl service;
        private MedStockOrderMapper orderMapper;
        private MedStockBatchMapper batchMapper;
        private IMedStockService stockService;
        private IMedStockWarnService stockWarnService;
    }
}
