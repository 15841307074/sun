package com.htyoudao.youdao.module.commodity.service.spuTag;

import com.htyoudao.youdao.module.commodity.controller.admin.spuTag.VO.CommodityTagReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;

import java.util.List;

public interface ICommodityTageService {
    void create(CommodityTagReqVO commodityTagReqVO);

    void update(CommodityTagReqVO commodityTagReqVO);

    CommodityTagReqVO getById(Long id);

    List<CommodityTagReqVO> selectList(CommodityTagReqVO commodityTagReqVO);

    void delById(Long id);

    List<CommodityTag> selectListByIds(List<Long> tagIds);
}
