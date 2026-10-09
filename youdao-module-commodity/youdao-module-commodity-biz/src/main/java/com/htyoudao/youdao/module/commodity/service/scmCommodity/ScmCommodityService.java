package com.htyoudao.youdao.module.commodity.service.scmCommodity;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.scmCommodiy.ScmCommodity;
import jakarta.validation.Valid;

import java.util.List;

public interface ScmCommodityService {
    List<ScmCommodity> selectListByStatiscsNames(List<String> nameList, Integer projectCode,Long warehouseId,int value);

    PageResult<ScmCommodityRespVO> selectPage(@Valid ScmCommodityPageReqVO scmCommodityPageReqVO,int value);

    /**
     * 供应链商品列表
     * @param scmCommodityDataReqVO
     * @return
     */
    List<ScmCommodityRespVO> selectByList(ScmCommodityDataReqVO scmCommodityDataReqVO);

    ScmCommodityInfoRespVO selectCommodityInfo(ScmCommodityInfoReqVO scmCommodityInfoReqVO, int value);
}
