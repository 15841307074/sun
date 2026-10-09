package com.htyoudao.youdao.module.order.core.submit.strategy;

import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;

/**
 * <p>
 * 订单提交策略基类
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
public interface IOrderSubmitStrategy<T extends SubmitReqVO> {

    /**
     * 获取订单来源
     *
     * @return
     */
    OrderSourceEnum getSource();

    /**
     * 提交订单
     *
     * @param reqVO
     * @return
     */
    SubmitResVO submit(T reqVO) throws Exception;
}
