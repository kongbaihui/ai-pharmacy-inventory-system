package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.MedStockBatch;
import com.ruoyi.system.domain.MedStockOrder;
import com.ruoyi.system.domain.MedStockOrderItem;
import com.ruoyi.system.mapper.MedStockBatchMapper;
import com.ruoyi.system.mapper.MedStockOrderMapper;
import com.ruoyi.system.service.IMedStockOrderService;
import com.ruoyi.system.service.IMedStockService;

/** 库存业务单Service业务层处理 */
@Service
public class MedStockOrderServiceImpl implements IMedStockOrderService
{
    @Autowired
    private MedStockOrderMapper orderMapper;

    @Autowired
    private MedStockBatchMapper batchMapper;

    @Autowired
    private IMedStockService stockService;

    @Override
    public List<MedStockOrder> selectMedStockOrderList(MedStockOrder order)
    {
        return orderMapper.selectMedStockOrderList(order);
    }

    @Override
    public MedStockOrder selectMedStockOrderById(Long orderId)
    {
        MedStockOrder order = orderMapper.selectMedStockOrderById(orderId);
        if (order != null)
        {
            order.setItemList(orderMapper.selectItemListByOrderId(orderId));
        }
        return order;
    }

    @Override
    @Transactional
    public int insertMedStockOrder(MedStockOrder order)
    {
        validateAndCalculate(order);
        order.setOrderNo(generateOrderNo(order.getOrderType()));
        order.setOrderStatus("0");
        order.setOperator(StringUtils.isEmpty(order.getOperator()) ? SecurityUtils.getUsername() : order.getOperator());
        order.setCreateBy(SecurityUtils.getUsername());
        int rows = orderMapper.insertMedStockOrder(order);
        insertItems(order);
        return rows;
    }

    @Override
    @Transactional
    public int updateMedStockOrder(MedStockOrder order)
    {
        if (order == null || order.getOrderId() == null)
        {
            throw new ServiceException("库存业务单不能为空");
        }
        MedStockOrder saved = orderMapper.selectMedStockOrderForUpdate(order.getOrderId());
        checkDraft(saved, "修改");
        order.setOrderType(saved.getOrderType());
        validateAndCalculate(order);
        order.setUpdateBy(SecurityUtils.getUsername());
        orderMapper.deleteItemsByOrderId(order.getOrderId());
        insertItems(order);
        return orderMapper.updateMedStockOrder(order);
    }

    @Override
    @Transactional
    public int confirmMedStockOrder(Long orderId)
    {
        MedStockOrder order = orderMapper.selectMedStockOrderForUpdate(orderId);
        checkDraft(order, "确认");
        order.setItemList(orderMapper.selectItemListByOrderId(orderId));
        validateAndCalculate(order);

        for (MedStockOrderItem item : order.getItemList())
        {
            if ("1".equals(order.getOrderType()))
            {
                confirmInbound(order, item);
            }
            else if ("2".equals(order.getOrderType()))
            {
                confirmOutbound(order, item);
            }
            else
            {
                confirmReturn(order, item);
            }
        }

        batchMapper.refreshBatchStatus();
        order.setOrderStatus("1");
        order.setConfirmBy(SecurityUtils.getUsername());
        order.setConfirmTime(new Date());
        order.setUpdateBy(SecurityUtils.getUsername());
        return orderMapper.updateOrderConfirmed(order);
    }

    private void confirmInbound(MedStockOrder order, MedStockOrderItem item)
    {
        MedStockBatch batch = orderMapper.selectBatchByMedAndNoForUpdate(item.getMedId(), item.getBatchNo());
        if (batch == null)
        {
            batch = new MedStockBatch();
            batch.setMedId(item.getMedId());
            batch.setSupplierId(order.getSupplierId());
            batch.setBatchNo(item.getBatchNo());
            batch.setProduceDate(item.getProduceDate());
            batch.setExpireDate(item.getExpireDate());
            batch.setBatchQty(item.getQuantity());
            batch.setRemainQty(item.getQuantity());
            batch.setPurchasePrice(item.getUnitPrice());
            batch.setInTime(new Date());
            batch.setBatchStatus("0");
            batch.setCreateBy(SecurityUtils.getUsername());
            batchMapper.insertMedStockBatch(batch);
        }
        else
        {
            if (batch.getSupplierId() != null && !batch.getSupplierId().equals(order.getSupplierId()))
            {
                throw new ServiceException("药品【" + item.getMedName() + "】批号已属于其他供应商");
            }
            if (!sameDate(batch.getExpireDate(), item.getExpireDate())
                    || !sameDate(batch.getProduceDate(), item.getProduceDate()))
            {
                throw new ServiceException("药品【" + item.getMedName() + "】相同批号的生产日期或有效期不一致");
            }
            if (orderMapper.increaseBatchByInbound(batch.getBatchId(), item.getQuantity(),
                    item.getUnitPrice(), SecurityUtils.getUsername()) != 1)
            {
                throw new ServiceException("入库批次数量更新失败");
            }
        }
        item.setBatchId(batch.getBatchId());
        orderMapper.updateItemBatchId(item.getItemId(), batch.getBatchId());
        stockService.adjustStock(item.getMedId(), batch.getBatchId(), item.getQuantity(), "1",
                order.getOrderNo(), order.getOrderId(), "药品入库");
    }

    private void confirmOutbound(MedStockOrder order, MedStockOrderItem item)
    {
        MedStockBatch batch = requireMatchingBatch(item);
        if (batch.getExpireDate() != null && batch.getExpireDate().before(today()))
        {
            throw new ServiceException("药品【" + item.getMedName() + "】批次已过期，不允许出库");
        }
        if (orderMapper.decreaseBatchForOutbound(batch.getBatchId(), item.getQuantity(),
                SecurityUtils.getUsername()) != 1)
        {
            throw new ServiceException("药品【" + item.getMedName() + "】批次库存不足，当前可用 " + batch.getRemainQty());
        }
        stockService.adjustStock(item.getMedId(), batch.getBatchId(), -item.getQuantity(), "2",
                order.getOrderNo(), order.getOrderId(), "药品出库");
    }

    private void confirmReturn(MedStockOrder order, MedStockOrderItem item)
    {
        MedStockBatch batch = requireMatchingBatch(item);
        if (orderMapper.increaseBatchForReturn(batch.getBatchId(), item.getQuantity(),
                SecurityUtils.getUsername()) != 1)
        {
            throw new ServiceException("药品【" + item.getMedName() + "】退库后将超过该批次累计入库数量");
        }
        stockService.adjustStock(item.getMedId(), batch.getBatchId(), item.getQuantity(), "3",
                order.getOrderNo(), order.getOrderId(), "科室药品退库");
    }

    private MedStockBatch requireMatchingBatch(MedStockOrderItem item)
    {
        if (item.getBatchId() == null)
        {
            throw new ServiceException("出库或退库明细必须选择库存批次");
        }
        MedStockBatch batch = orderMapper.selectBatchForUpdate(item.getBatchId());
        if (batch == null || !batch.getMedId().equals(item.getMedId()))
        {
            throw new ServiceException("库存批次不存在或与药品不匹配");
        }
        return batch;
    }

    @Override
    @Transactional
    public int deleteMedStockOrderByIds(Long[] orderIds)
    {
        if (orderIds == null || orderIds.length == 0)
        {
            return 0;
        }
        int rows = 0;
        for (Long orderId : orderIds)
        {
            MedStockOrder order = orderMapper.selectMedStockOrderForUpdate(orderId);
            checkDraft(order, "删除");
            orderMapper.deleteItemsByOrderId(orderId);
            rows += orderMapper.deleteMedStockOrderById(orderId);
        }
        return rows;
    }

    private void validateAndCalculate(MedStockOrder order)
    {
        if (order == null || !("1".equals(order.getOrderType()) || "2".equals(order.getOrderType())
                || "3".equals(order.getOrderType())))
        {
            throw new ServiceException("业务类型不正确");
        }
        if ("1".equals(order.getOrderType()) && order.getSupplierId() == null)
        {
            throw new ServiceException("入库单必须选择供应商");
        }
        if ("1".equals(order.getOrderType()) && orderMapper.countEnabledSupplier(order.getSupplierId()) != 1)
        {
            throw new ServiceException("供应商不存在或已停用");
        }
        if (("2".equals(order.getOrderType()) || "3".equals(order.getOrderType()))
                && StringUtils.isEmpty(order.getDepartment()))
        {
            throw new ServiceException("出库或退库单必须填写领用/退回科室");
        }
        List<MedStockOrderItem> items = order.getItemList();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("业务单至少需要一条药品明细");
        }

        long totalQty = 0L;
        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<String> uniqueLines = new HashSet<>();
        for (MedStockOrderItem item : items)
        {
            if (item.getMedId() == null || item.getQuantity() == null || item.getQuantity() <= 0)
            {
                throw new ServiceException("明细中的药品和正数数量不能为空");
            }
            if (orderMapper.countEnabledMed(item.getMedId()) != 1)
            {
                throw new ServiceException("明细中的药品不存在或已停用");
            }
            if ("1".equals(order.getOrderType()))
            {
                if (StringUtils.isEmpty(item.getBatchNo()) || item.getExpireDate() == null)
                {
                    throw new ServiceException("入库明细必须填写生产批号和有效期");
                }
                if (item.getProduceDate() != null && item.getProduceDate().after(item.getExpireDate()))
                {
                    throw new ServiceException("生产日期不能晚于有效期");
                }
            }
            else if (item.getBatchId() == null)
            {
                throw new ServiceException("出库或退库明细必须选择批次");
            }

            String uniqueKey = item.getMedId() + "_"
                    + ("1".equals(order.getOrderType()) ? item.getBatchNo() : item.getBatchId());
            if (!uniqueLines.add(uniqueKey))
            {
                throw new ServiceException("同一药品批次不能重复添加");
            }
            BigDecimal price = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
            if (price.signum() < 0)
            {
                throw new ServiceException("单价不能小于0");
            }
            item.setUnitPrice(price);
            item.setAmount(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            totalQty += item.getQuantity();
            totalAmount = totalAmount.add(item.getAmount());
        }
        order.setOrderDate(order.getOrderDate() == null ? new Date() : order.getOrderDate());
        order.setTotalQty(totalQty);
        order.setTotalAmount(totalAmount);
    }

    private void insertItems(MedStockOrder order)
    {
        for (MedStockOrderItem item : order.getItemList())
        {
            item.setOrderId(order.getOrderId());
            orderMapper.insertMedStockOrderItem(item);
        }
    }

    private void checkDraft(MedStockOrder order, String action)
    {
        if (order == null)
        {
            throw new ServiceException("库存业务单不存在");
        }
        if (!"0".equals(order.getOrderStatus()))
        {
            throw new ServiceException("业务单【" + order.getOrderNo() + "】已确认，不允许" + action);
        }
    }

    private String generateOrderNo(String orderType)
    {
        String prefix = "1".equals(orderType) ? "RK" : ("2".equals(orderType) ? "CK" : "TK");
        return prefix + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private Date today()
    {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return format.parse(format.format(new Date()));
        }
        catch (Exception e)
        {
            return new Date();
        }
    }

    private boolean sameDate(Date first, Date second)
    {
        if (first == null || second == null)
        {
            return first == second;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(first).equals(format.format(second));
    }
}
