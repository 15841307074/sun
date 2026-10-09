package com.htyoudao.youdao.module.order.core.submit.strategy.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.submit.strategy.AbstractIOrderSubmitStrategy;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 付款单，小程序付款单
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Slf4j
@Component
public class XPaymentOrderStrategy extends AbstractIOrderSubmitStrategy<SubmitReqVO> {

    @Override
    public OrderSourceEnum getSource() {
        return OrderSourceEnum.X_PAYMENT_ORDER;
    }

    @Override
    protected void validateSpecificBefore(SubmitReqVO reqVO) {
//        super.checkRepeatToken(reqVO);
    }

    @Override
    protected void validateSpecificAfter(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        super.validateSpecificAfter(reqVO, cacheData);
        super.validateExchangeCommodityCoupon(cacheData);

        //门店未在堂食营业时间段内
        if (ObjectUtils.isEmpty(cacheData.getStoreHours()) || !com.htyoudao.youdao.framework.common.util.date.DateUtils.isBusinessOpen(Arrays.asList(cacheData.getStoreHours().split(",")))) {
            throw exception(ORDER_T_STORE_NOT_WORK);
        }
        //小程序门店状态（0 正常营业 1  闭店）
        if (OrderConstants.YES.equals(cacheData.getMiniproStatus())) {
            throw exception(ORDER_STORE_NOT_MINIPRO);
        }

        //门店是否支持不付款下单 （0支持 1 不支持）
        if (OrderConstants.NO.equals(cacheData.getStoreWithoutPayment())) {
            throw exception(ORDER_STORE_WITHOUT_PAYMENT);
        }
    }

    @Override
    protected String getPickUpCode(Long storeId) {
        return super.getPickUpCode(storeId);
    }

    @Override
    @Transactional
    public SubmitResVO submit(SubmitReqVO reqVO) throws Exception {
        SubmitResVO result = super.submit(reqVO);

        // TODO 尽情发挥...

        return result;
    }

    @Override
    protected void postProcess(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheDataDTO, BzOrderDO bzOrderDO) {
        super.postProcess(reqVO, cacheDataDTO, bzOrderDO);
        //使用优惠券
        if (!ObjectUtils.isEmpty(cacheDataDTO.getUserCouponId())) {
            super.usedCoupon(cacheDataDTO);
        }
        // 清除缓存
        stringRedisTemplate.delete(reqVO.getCacheKey());

        log.info("==> 【小程序付款订】单提交成功 | orderSn {}", cacheDataDTO.getOrderSn());
    }
}
