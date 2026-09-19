package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MedInfo;

/**
 * 药品信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public interface MedInfoMapper 
{
    /**
     * 查询药品信息
     * 
     * @param medId 药品信息主键
     * @return 药品信息
     */
    public MedInfo selectMedInfoByMedId(Long medId);

    /**
     * 查询药品信息列表（关联分类、供应商与库存数量）
     * 
     * @param medInfo 药品信息
     * @return 药品信息集合
     */
    public List<MedInfo> selectMedInfoList(MedInfo medInfo);

    /**
     * 新增药品信息
     * 
     * @param medInfo 药品信息
     * @return 结果
     */
    public int insertMedInfo(MedInfo medInfo);

    /**
     * 修改药品信息
     * 
     * @param medInfo 药品信息
     * @return 结果
     */
    public int updateMedInfo(MedInfo medInfo);

    /**
     * 删除药品信息
     * 
     * @param medId 药品信息主键
     * @return 结果
     */
    public int deleteMedInfoByMedId(Long medId);

    /**
     * 批量删除药品信息
     * 
     * @param medIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedInfoByMedIds(Long[] medIds);

    /**
     * 统计分类下的药品数量
     * 
     * @param categoryId 药品分类主键
     * @return 药品数量
     */
    public int countMedByCategoryId(Long categoryId);

    /**
     * 统计供应商下的药品数量
     * 
     * @param supplierId 供应商主键
     * @return 药品数量
     */
    public int countMedBySupplierId(Long supplierId);

    /**
     * 校验药品编码是否唯一
     * 
     * @param medCode 药品编码
     * @return 药品信息
     */
    public MedInfo checkMedCodeUnique(String medCode);
}
