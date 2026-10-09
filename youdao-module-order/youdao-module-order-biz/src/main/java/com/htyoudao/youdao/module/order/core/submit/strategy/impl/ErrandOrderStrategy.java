package com.htyoudao.youdao.module.order.core.submit.strategy.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.PriceCalculatorV2Service;
import com.htyoudao.youdao.module.order.core.submit.strategy.AbstractIOrderSubmitStrategy;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import com.htyoudao.youdao.module.order.enums.OrderTypeEnum;
import com.htyoudao.youdao.module.order.enums.PaymentMethodEnum;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 代取单，小程序强制付款单。
 * </p>
 *
 * @author zhangjihe
 * @since 2026-06-03
 */
@Slf4j
@Component
public class ErrandOrderStrategy extends AbstractIOrderSubmitStrategy<SubmitReqVO> {

    @Resource
    private Validator validator;
    @Resource
    private PriceCalculatorV2Service priceCalculatorV2Service;

    @Override
    public OrderSourceEnum getSource() {
        return OrderSourceEnum.ERRAND_ORDER;
    }

    @Override
    protected void validateSpecificBefore(SubmitReqVO reqVO) {
//        super.checkRepeatToken(reqVO);
        if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), reqVO.getOrderType())) {
            throw exception(ORDER_WRONG_TYPE);
        }
        priceCalculatorV2Service.validateErrandOrderTimeIfEnabled();
        if (Objects.equals(PaymentMethodEnum.CASH.getCode(), reqVO.getPaymentCode())) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "代取订单不支持不付款下单");
        }
        if (reqVO.getErrandGenderLimit() == null || reqVO.getErrandGenderLimit() < 0 || reqVO.getErrandGenderLimit() > 2) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "跑腿员性别限制不合法");
        }
        if (reqVO.getReceiverInfo() == null) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "收货信息不能为空");
        }

        Set<ConstraintViolation<SubmitReqVO.ReceiverInfoVO>> violations = validator.validate(reqVO.getReceiverInfo());
        if (violations == null) {
            violations = Collections.emptySet();
        }
        if (!violations.isEmpty()) {
            for (ConstraintViolation<SubmitReqVO.ReceiverInfoVO> violation : violations) {
                throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), violation.getMessage());
            }
        }
    }

    @Override
    protected void validateSpecificAfter(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        super.validateSpecificAfter(reqVO, cacheData);

        if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), cacheData.getOrderType())) {
            throw exception(ORDER_WRONG_TYPE);
        }

        //门店未在堂食营业时间段内
        if (ObjectUtils.isEmpty(cacheData.getStoreHours()) || !com.htyoudao.youdao.framework.common.util.date.DateUtils.isBusinessOpen(Arrays.asList(cacheData.getStoreHours().split(",")))) {
            throw exception(ORDER_T_STORE_NOT_WORK);
        }
        //小程序门店状态（0 正常营业 1 闭店）
        if (OrderConstants.YES.equals(cacheData.getMiniproStatus())) {
            throw exception(ORDER_STORE_NOT_MINIPRO);
        }
        //沿用小程序付款单校验：门店不支持付款下单时不可提交代取单。
        if (OrderConstants.NO.equals(cacheData.getStoreWithoutPayment())) {
            throw exception(ORDER_STORE_WITHOUT_PAYMENT);
        }
        if (!OrderConstants.NO.equals(cacheData.getCampusDeliveryStatus())) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "门店未开启代取订单");
        }
        //起送费
        if (ObjectUtils.isNotEmpty(cacheData.getMinimumDeliveryFeeIsOk()) && OrderConstants.NO.equals(cacheData.getMinimumDeliveryFeeIsOk())) {
            throw new ServiceException(new ErrorCode(ORDER_AMOUNT_NOT_ENOUGH.getCode(), String.format(ORDER_AMOUNT_NOT_ENOUGH.getMsg(), cacheData.getMinimumDeliveryFee())));
        }
        //餐费需大于门店跑腿补贴
        if (ObjectUtils.isNotEmpty(cacheData.getErrandStoreSubsidyIsOk()) && OrderConstants.NO.equals(cacheData.getErrandStoreSubsidyIsOk())) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "餐费需大于门店跑腿补贴金额");
        }
    }

    @Override
    protected BzOrderDO buildBzOrderDO(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        BzOrderDO bzOrderDO = super.buildBzOrderDO(reqVO, cacheData);

        bzOrderDO.setOrderType(OrderTypeEnum.ERRAND.getCode());
        bzOrderDO.setErrandGenderLimit(reqVO.getErrandGenderLimit());
        bzOrderDO.setReceiverName(reqVO.getReceiverInfo().getReceiverName());
        bzOrderDO.setReceiverAreaInfo(cacheData.getStoreName());
        bzOrderDO.setReceiverAddress(reqVO.getReceiverInfo().getReceiverAddress());
        bzOrderDO.setReceiverMobile(reqVO.getReceiverInfo().getReceiverMobile());
        bzOrderDO.setDeliveryId(null);
        bzOrderDO.setDeliveryName(null);
        bzOrderDO.setDeliveryPhone(null);

        return bzOrderDO;
    }

    @Override
    @Transactional
    public SubmitResVO submit(SubmitReqVO reqVO) throws Exception {
        return super.submit(reqVO);
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

        log.info("==> 【代取订单】提交成功 | orderSn {}", cacheDataDTO.getOrderSn());
    }
}
