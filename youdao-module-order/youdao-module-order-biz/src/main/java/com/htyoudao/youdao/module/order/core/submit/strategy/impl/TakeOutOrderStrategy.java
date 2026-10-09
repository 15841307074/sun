package com.htyoudao.youdao.module.order.core.submit.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.submit.strategy.AbstractIOrderSubmitStrategy;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import com.htyoudao.youdao.module.order.enums.OrderTypeEnum;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 外卖单，强制付款单
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Slf4j
@Component
public class TakeOutOrderStrategy extends AbstractIOrderSubmitStrategy<SubmitReqVO> {

    @Resource
    private Validator validator;
    @DubboReference(timeout = 5000)
    private StoreApi storeApi;

    @Override
    public OrderSourceEnum getSource() {
        return OrderSourceEnum.TAKE_OUT_ORDER;
    }

    @Override
    protected void validateSpecificBefore(SubmitReqVO reqVO) {
//        super.checkRepeatToken(reqVO);
        Set<ConstraintViolation<SubmitReqVO.ReceiverInfoVO>> violations = validator.validate(reqVO.getReceiverInfo());

        if (violations == null) {
            violations = Collections.emptySet();
        }

        if (!violations.isEmpty()) {
            for (ConstraintViolation<SubmitReqVO.ReceiverInfoVO> violation : violations) {
                throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), violation.getMessage());
            }
        }
    }

    @Override
    protected void validateSpecificAfter(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        super.validateSpecificAfter(reqVO, cacheData);

        StoreDTO storeDTO = storeApi.getStoreByStoreId(reqVO.getStoreId()).getCheckedData();
        if (ObjectUtils.isEmpty(storeDTO)) {
            throw exception(ORDER_GET_STORE_FAIL);
        }
        log.info("==>【外卖订单提交】门店信息 {}", JSON.toJSONString(storeDTO));

        //是否支持外卖
        if(OrderConstants.YES.equals(storeDTO.getStoreTakeaway())){
            throw exception(ORDER_STORE_WITHOUT_DELIVERY);
        }

        //门店未在外卖营业时间段内
        if (ObjectUtils.isEmpty(storeDTO.getDeliveryTime()) || !DateUtils.isBusinessOpen(Arrays.asList(storeDTO.getDeliveryTime().split(",")))) {
            throw exception(ORDER_W_STORE_NOT_WORK);
        }

        //小程序门店状态（0 正常营业 1  闭店）
        if (OrderConstants.YES.equals(storeDTO.getOrderStoreType())) {
            throw exception(ORDER_STORE_NOT_MINIPRO);
        }

        //起送费
        if(ObjectUtils.isNotEmpty(cacheData.getMinimumDeliveryFeeIsOk()) && OrderConstants.NO.equals(cacheData.getMinimumDeliveryFeeIsOk())){
            throw new ServiceException(new ErrorCode(ORDER_AMOUNT_NOT_ENOUGH.getCode(), String.format(ORDER_AMOUNT_NOT_ENOUGH.getMsg(), cacheData.getMinimumDeliveryFee())));
        }
    }

    @Override
    protected BzOrderDO buildBzOrderDO(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        BzOrderDO bzOrderDO = super.buildBzOrderDO(reqVO, cacheData);

        bzOrderDO.setReceiverName(reqVO.getReceiverInfo().getReceiverName());
        bzOrderDO.setReceiverAddress(reqVO.getReceiverInfo().getReceiverAddress());
        bzOrderDO.setReceiverMobile(reqVO.getReceiverInfo().getReceiverMobile());
        bzOrderDO.setOrderType(OrderTypeEnum.TAKEAWAY.getCode());

        return bzOrderDO;
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
    }
}
