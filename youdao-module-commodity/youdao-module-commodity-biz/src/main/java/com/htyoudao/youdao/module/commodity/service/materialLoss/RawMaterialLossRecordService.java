package com.htyoudao.youdao.module.commodity.service.materialLoss;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO.SplitCommodityMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLoss.VO.SchoolProgramCommoditySplitMaterialDataVO;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLoss.VO.SchoolProgramCommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.*;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLossRecordDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 损耗记录Service
 */
public interface RawMaterialLossRecordService extends IService<RawMaterialLossRecordDO> {
    /**
     * 损耗记录分页列表
     * @param pageReq
     * @return
     */
    PageResult<MaterialLossRecordPageVO> lossRecordPageList(MaterialLossRecordPageReq pageReq);

    /**
     * 删除损耗记录
     * @param lossDeleteReq
     * @return
     */
    Boolean deleteLossRecord(MaterialLossDeleteReq lossDeleteReq);

    /**
     * 新增损耗记录
     * @param saveReq
     * @return
     */
    Boolean saveLossRecord(@Valid MaterialLossSaveReq saveReq);


    /**
     * 根据门店ID,盘点ID 获取对应的损耗单ID集合
     * @param storeId 门店ID
     * @param takeId 为空时 查null的
     * @return
     */
    List<Long> lossIdsByStoreTake(Long storeId, Long takeId);


    /**
     * 通过损耗ID 获取损耗的数量 与 金额
     *
     * @param lossIds
     * @return key: 损耗数量:损耗金额
     */
    Map<Long, Pair<BigDecimal, BigDecimal>> getLossMap(List<Long> lossIds);

    /**
     * 更新所有未盘点的盘点ID  入参 盘点ID
     * @param takeId
     * @return
     */
    BigDecimal updateUninventoriedLossRecord(Long takeId,Long storeId);

    /**
     * 获取损耗率
     */
    BigDecimal getAttritionRate(Long storeId);


    /*
        takeId，损耗率 更新损耗列表
     */
    BigDecimal updateUninventoriedLossRecord(Long takeId,Long storeId,BigDecimal attritionRate);


    /**
     * 获取损耗记录类型列表接口
     * @return
     */
    List<MaterialTypeVo> lossTypeList();

    /**
     *
     * @param commoditySplitMaterialReqVO
     * @return
     */
    List<MaterialListRespVo> modifyCommodity(CommoditySplitMaterialReqVO commoditySplitMaterialReqVO);

    /**
     * 根据商品名称拆原料并扣减库存后返回
     * @return
     */
    List<MaterialListRespVo> splitCommodityMaterial(List<SplitCommodityMaterialReqVO> splitCommodityMaterialReqVO);

    Boolean schoolProgramModifyCommodity(SchoolProgramCommoditySplitMaterialReqVO schoolProgramCommoditySplitMaterialReqVO);

    SchoolProgramCommoditySplitMaterialDataVO selectInfo(Long id);
}
