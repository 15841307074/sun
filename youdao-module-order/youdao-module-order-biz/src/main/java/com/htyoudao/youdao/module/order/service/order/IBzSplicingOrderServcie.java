package com.htyoudao.youdao.module.order.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SplicingOrderMemberAddReqVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzSplicingOrderDO;

import java.util.Map;

/**
 * <p>
 * 拼单
 * </p>
 *
 * @author zhangjihe
 * @since 2024-12-24
 */
public interface IBzSplicingOrderServcie extends IService<BzSplicingOrderDO> {

    Map<String, Object> mainSelect(String mainId, String openId, Long storeId);

    void mainContinue(String mainId, String openId, Long storeId);

    String getCommodity(String mainId, String openId, Long storeId);

    void mainLock(String mainId, String openId);

    void mainCancel(String mainId, String openId);

    void commodityChange(SplicingOrderMemberAddReqVO reqVO);

    void feignTest();
}
