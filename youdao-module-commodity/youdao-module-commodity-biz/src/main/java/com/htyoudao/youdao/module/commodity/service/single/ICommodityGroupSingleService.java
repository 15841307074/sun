package com.htyoudao.youdao.module.commodity.service.single;


import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.CommodityUNameReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusUpdateReqVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityGroupSingle;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface ICommodityGroupSingleService {

    void saveBatch(List<CommodityGroupSingle> commodityGroupSingleList);

    void deleteBySpuId(Long commodityId);

    List<CommodityGroupSingle> selectBySpuId(Long commodityId);

    List<CommodityGroupSingle> selectBySpuIds(List<Long> commodityIds);

    void updateUpInSpuIds(List<Long> commodityIds);

    List<CommodityGroupSingle> selectByGroupIds(List<Long> setmealIds);

    void updateDownBySpuIds(List<Long> commodityIds);

    void updateByIds(List<CommodityGroupSingle> singles);

    void deleteBySpuIds(List<Long> commodityIds);

    void updateSpuNameBySpuId(CommodityUNameReqVo uNameReqVo);

    List<CommodityGroupSingle> selectByChooseViewAndSpuId(Long commodityId, Integer chooseView);


    void updateDownBySpuId(Integer chooseView, Long commodityId);

    void updateUpBySpuId(Integer chooseView, Long commodityId);

    List<CommodityGroupSingle> selectByCommodityIds(List<Long> setmealIds);

    List<CommodityGroupSingle> selectBySpuOriginalIds(List<Long> commodityIds);


    void changeImageAndName(CommoditySpusUpdateReqVo updateReqVo);

    void changeName(CommodityUNameReqVo uNameReqVo);

    List<CommodityGroupSingle> selectByChooseViewAndSpuIdForDown(Long commodityId, Integer chooseView);

    void updateUpOrDownBySpuId(Integer chooseView, List<Long> singleUpId, Integer code);

    void emitSyncToSubProducts(String flavorJson, Long commodityId);

    void emitSyncEmptyToSubProducts(Long commodityId);
}
