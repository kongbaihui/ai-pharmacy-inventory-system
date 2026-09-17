package com.ruoyi.system.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.MedStockBatchMapper;
import com.ruoyi.system.mapper.MedStockCheckItemMapper;
import com.ruoyi.system.mapper.MedStockCheckMapper;
import com.ruoyi.system.domain.MedStockCheck;
import com.ruoyi.system.domain.MedStockCheckItem;
import com.ruoyi.system.service.IMedStockCheckService;
import com.ruoyi.system.service.IMedStockService;

/**
 * 库存盘点Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
@Service
public class MedStockCheckServiceImpl implements IMedStockCheckService 
{
    @Autowired
    private MedStockCheckMapper medStockCheckMapper;

    @Autowired
    private MedStockCheckItemMapper medStockCheckItemMapper;

    @Autowired
    private MedStockBatchMapper medStockBatchMapper;

    @Autowired
    private IMedStockService medStockService;

    /**
     * 查询库存盘点
     * 
     * @param checkId 库存盘点主键
     * @return 库存盘点
     */
    @Override
    public MedStockCheck selectMedStockCheckByCheckId(Long checkId)
    {
        MedStockCheck medStockCheck = medStockCheckMapper.selectMedStockCheckByCheckId(checkId);
        if (medStockCheck != null)
        {
            MedStockCheckItem item = new MedStockCheckItem();
            item.setCheckId(checkId);
            medStockCheck.setMedStockCheckItemList(medStockCheckItemMapper.selectMedStockCheckItemList(item));
        }
        return medStockCheck;
    }

    /**
     * 查询库存盘点列表
     * 
     * @param medStockCheck 库存盘点
     * @return 库存盘点
     */
    @Override
    public List<MedStockCheck> selectMedStockCheckList(MedStockCheck medStockCheck)
    {
        return medStockCheckMapper.selectMedStockCheckList(medStockCheck);
    }

    /**
     * 查询盘点明细列表
     * 
     * @param medStockCheckItem 盘点明细
     * @return 盘点明细集合
     */
    @Override
    public List<MedStockCheckItem> selectMedStockCheckItemList(MedStockCheckItem medStockCheckItem)
    {
        return medStockCheckItemMapper.selectMedStockCheckItemList(medStockCheckItem);
    }

    /**
     * 按当前批次库存生成盘点明细（账面数量）
     * 
     * @param medStockCheck 库存盘点
     * @return 盘点明细集合
     */
    @Override
    public List<MedStockCheckItem> selectBookStockItemList(MedStockCheck medStockCheck)
    {
        return medStockCheckItemMapper.selectBookStockItemList(medStockCheck);
    }

    /**
     * 新增库存盘点
     * 
     * @param medStockCheck 库存盘点
     * @return 结果
     */
    @Override
    @Transactional
    public int insertMedStockCheck(MedStockCheck medStockCheck)
    {
        medStockCheck.setCheckNo(generateCheckNo());
        if (medStockCheck.getCheckDate() == null)
        {
            medStockCheck.setCheckDate(new Date());
        }
        if (StringUtils.isEmpty(medStockCheck.getCheckStatus()))
        {
            medStockCheck.setCheckStatus("0");
        }
        if (StringUtils.isEmpty(medStockCheck.getCheckUser()))
        {
            medStockCheck.setCheckUser(SecurityUtils.getUsername());
        }
        medStockCheck.setCreateBy(SecurityUtils.getUsername());
        int rows = medStockCheckMapper.insertMedStockCheck(medStockCheck);
        insertMedStockCheckItem(medStockCheck);
        return rows;
    }

    /**
     * 修改库存盘点
     * 
     * @param medStockCheck 库存盘点
     * @return 结果
     */
    @Override
    @Transactional
    public int updateMedStockCheck(MedStockCheck medStockCheck)
    {
        MedStockCheck check = medStockCheckMapper.selectMedStockCheckByCheckId(medStockCheck.getCheckId());
        if (check == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if ("3".equals(check.getCheckStatus()))
        {
            throw new ServiceException("盘点单已审核，不允许修改");
        }
        medStockCheckMapper.deleteMedStockCheckItemByCheckId(medStockCheck.getCheckId());
        insertMedStockCheckItem(medStockCheck);
        medStockCheck.setUpdateBy(SecurityUtils.getUsername());
        return medStockCheckMapper.updateMedStockCheck(medStockCheck);
    }

    /**
     * 保存盘点明细：前端已录入实盘数量时保存录入数据，否则按当前批次库存生成账面数据
     */
    private void insertMedStockCheckItem(MedStockCheck medStockCheck)
    {
        List<MedStockCheckItem> list = medStockCheck.getMedStockCheckItemList();
        if (list == null || list.isEmpty())
        {
            list = medStockCheckItemMapper.selectBookStockItemList(medStockCheck);
        }
        if (list == null || list.isEmpty())
        {
            return;
        }
        for (MedStockCheckItem item : list)
        {
            item.setCheckId(medStockCheck.getCheckId());
            calcDiff(item);
        }
        medStockCheckMapper.batchMedStockCheckItem(list);
    }

    /**
     * 计算盈亏数量与盈亏类型
     */
    private void calcDiff(MedStockCheckItem item)
    {
        long bookQty = item.getBookQty() == null ? 0L : item.getBookQty();
        long realQty = item.getRealQty() == null ? 0L : item.getRealQty();
        long diffQty = realQty - bookQty;
        item.setBookQty(bookQty);
        item.setRealQty(realQty);
        item.setDiffQty(diffQty);
        if (diffQty > 0)
        {
            item.setDiffType("1");
        }
        else if (diffQty < 0)
        {
            item.setDiffType("2");
        }
        else
        {
            item.setDiffType("0");
        }
    }

    /**
     * 批量删除库存盘点
     * 
     * @param checkIds 需要删除的库存盘点主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMedStockCheckByCheckIds(Long[] checkIds)
    {
        for (Long checkId : checkIds)
        {
            checkAllowDelete(checkId);
        }
        medStockCheckMapper.deleteMedStockCheckItemByCheckIds(checkIds);
        return medStockCheckMapper.deleteMedStockCheckByCheckIds(checkIds);
    }

    /**
     * 删除库存盘点信息
     * 
     * @param checkId 库存盘点主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMedStockCheckByCheckId(Long checkId)
    {
        checkAllowDelete(checkId);
        medStockCheckMapper.deleteMedStockCheckItemByCheckId(checkId);
        return medStockCheckMapper.deleteMedStockCheckByCheckId(checkId);
    }

    private void checkAllowDelete(Long checkId)
    {
        MedStockCheck check = medStockCheckMapper.selectMedStockCheckByCheckId(checkId);
        if (check != null && "3".equals(check.getCheckStatus()))
        {
            throw new ServiceException("盘点单【" + check.getCheckNo() + "】已审核，不允许删除");
        }
    }

    /**
     * 盘点审核：按盈亏数量调整库存并登记库存流水
     * 
     * @param checkId 库存盘点主键
     * @return 调整的明细条数
     */
    @Override
    @Transactional
    public int auditMedStockCheck(Long checkId)
    {
        MedStockCheck check = medStockCheckMapper.selectMedStockCheckByCheckId(checkId);
        if (check == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if ("3".equals(check.getCheckStatus()))
        {
            throw new ServiceException("盘点单【" + check.getCheckNo() + "】已审核完成，无需重复审核");
        }
        MedStockCheckItem query = new MedStockCheckItem();
        query.setCheckId(checkId);
        List<MedStockCheckItem> items = medStockCheckItemMapper.selectMedStockCheckItemList(query);
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("盘点单没有明细数据，无法审核");
        }
        int adjustCount = 0;
        for (MedStockCheckItem item : items)
        {
            calcDiff(item);
            medStockCheckItemMapper.updateMedStockCheckItem(item);
            if (item.getDiffQty() == null || item.getDiffQty() == 0L)
            {
                continue;
            }
            medStockService.adjustStock(item.getMedId(), item.getBatchId(), item.getDiffQty(), "4",
                    check.getCheckNo(), checkId, "库存盘点调整");
            if (item.getBatchId() != null)
            {
                medStockBatchMapper.updateBatchRemainQty(item.getBatchId(), item.getDiffQty());
            }
            adjustCount++;
        }
        MedStockCheck update = new MedStockCheck();
        update.setCheckId(checkId);
        update.setCheckStatus("3");
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(new Date());
        update.setUpdateBy(SecurityUtils.getUsername());
        medStockCheckMapper.updateMedStockCheck(update);
        return adjustCount;
    }

    /**
     * 生成盘点单号，规则为 PD + 日期 + 4 位流水号
     */
    private String generateCheckNo()
    {
        String prefix = "PD" + new SimpleDateFormat("yyyyMMdd").format(new Date());
        String maxNo = medStockCheckMapper.selectMaxCheckNo(prefix);
        int sequence = 1;
        if (StringUtils.isNotEmpty(maxNo) && maxNo.length() > prefix.length())
        {
            sequence = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
        }
        return prefix + String.format("%04d", sequence);
    }
}
