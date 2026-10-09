package com.htyoudao.youdao.module.order.core.submit.strategy.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.mq.rabbitmq.enums.RabbitMQConstant;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.member.api.point.PointLogApi;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.submit.strategy.AbstractIOrderSubmitStrategy;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 现金单 小程序不付款单
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Slf4j
@Component
public class XCashOrderStrategy extends AbstractIOrderSubmitStrategy<SubmitReqVO> {

    @DubboReference
    private PointLogApi pointLogApi;

    @Override
    public OrderSourceEnum getSource() {
        return OrderSourceEnum.X_CASH_ORDER;
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
        if (OrderConstants.YES.equals(cacheData.getStoreWithoutPayment()) && cacheData.getPayAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(ORDER_STORE_NOT_WITHOUT_PAYMENT);
        }
    }

    @Override
    protected BzOrderDO buildBzOrderDO(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        BzOrderDO bzOrderDO = super.buildBzOrderDO(reqVO, cacheData);
        //现金单直接置为制作中
        bzOrderDO.setOrderState(OrderStateEnum.MAKING.getCode());
        bzOrderDO.setPayTime(LocalDateTime.now());
        bzOrderDO.setPickUpNum(super.getPickUpCode(reqVO.getStoreId()));

        try {
            //更新最后下单时间带出的相关信息
            bzOrderService.updateFinalOrderFinishTime(cacheData.getMemberId(), bzOrderDO);
        } catch (Exception e) {
            log.error("==> 不付款下单更新最后下单时间失败 {}", bzOrderDO.getOrderSn(), e);
        }

        return bzOrderDO;
    }

    @Override
    @Transactional
    public SubmitResVO submit(SubmitReqVO reqVO) throws Exception {
        SubmitResVO result = super.submit(reqVO);

        // TODO ...

        return result;
    }

    @Override
    protected void postProcess(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheDataDTO, BzOrderDO bzOrderDO) {
        //使用优惠券
        if (!ObjectUtils.isEmpty(cacheDataDTO.getUserCouponId())) {
            super.usedCoupon(cacheDataDTO);
        }

        // 清除缓存
        stringRedisTemplate.delete(reqVO.getCacheKey());

        strongExecutor.submit(() -> {
            OrderDetailDTO detail = super.getOrderDetail(cacheDataDTO, bzOrderDO);

            try {
                log.info("==> 【小程序不付款下单】扣减原材料库存 {}", bzOrderDO.getOrderSn());
                bzOrderService.changeStock(bzOrderDO, detail, StockChangeEnum.SALE);
            } catch (Exception e) {
                log.error("==> 【小程序不付款下单】扣减原材料库存 失败 {}", bzOrderDO.getOrderSn(), e);
            }
        });

        weakExecutor.submit(() -> {
            //发MQ
            rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, cacheDataDTO.getStoreId()), "", cacheDataDTO.getOrderSn());

            try {
                log.info("==> 【小程序快速下单】kafka发消息 {}", bzOrderDO.getOrderSn());
                bzOrderService.notifyOrder(bzOrderDO.getOrderSn(), "INSERT");
            } catch (Exception e) {
                log.error("==> 【小程序快速下单】kafka发消息 失败 {}", bzOrderDO.getOrderSn(), e);
            }

            try {
                log.info("==> 【小程序不付款下单】缓存商品销量+ orderSn {}", cacheDataDTO.getOrderSn());
                cacheDataDTO.getCommodityInfos().forEach(commodityInfo -> {
                    if (commodityInfo.getCommodityId() == null) {
                        return;
                    }
                    if (OrderConstants.YES.equals(commodityInfo.getIsPurchase())) {
                        bzOrderService._incrementPurchaseSales(commodityInfo.getCommodityId(), false);
                    } else {
                        bzOrderService._incrementProductSales(commodityInfo.getCommodityId(), commodityInfo.getCopies(), false);
                    }
                });
            } catch (Exception e) {
                log.error("==> 【小程序不付款下单】缓存商品销量+ 失败 orderSn {}", cacheDataDTO.getOrderSn(), e);
            }
        });
    }
}
