package com.htyoudao.youdao.module.order.core.submit.strategy.impl;

import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.KioskSubmitReqVO;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.SettlementReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.core.calc.v1.PriceCalculatorService;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.PriceCalculatorV2Service;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.KioskSubmitReqV2VO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import com.htyoudao.youdao.module.order.core.submit.strategy.AbstractIOrderSubmitStrategy;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_SETTLEMENT_INFO_NOT_NULL;

/**
 * <p>
 * 现金单 包括点餐机快速下单
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Slf4j
@Component
public class KCashOrderStrategy extends AbstractIOrderSubmitStrategy<KioskSubmitReqV2VO> {

    @Resource
    private PriceCalculatorV2Service priceCalculatorV2Service;

    @Override
    public OrderSourceEnum getSource() {
        return OrderSourceEnum.K_CASH_ORDER;
    }

    @Override
    protected void validateSpecificBefore(KioskSubmitReqV2VO reqVO) {
        //点餐机下单不走结算页，直接在入参中带上商品信息提交订单
        if (reqVO.getSettlementInfo() == null) {
            throw exception(ORDER_SETTLEMENT_INFO_NOT_NULL);
        }
    }

    @Override
    protected void validateSpecificAfter(KioskSubmitReqV2VO reqVO, CalculateCacheDataV2DTO cacheData) {
        //点餐机下单直接略过 ，不走super
    }

    @Override
    protected CalculateCacheDataV2DTO getCacheData(KioskSubmitReqV2VO reqVO) throws Exception {
        SettlementReqV2VO settlementInfo = reqVO.getSettlementInfo();
        settlementInfo.setOrderType(reqVO.getOrderType());
        settlementInfo.setIsDc(true);
        //重新去计算商品价格，并返回CalculateCacheData对象
        CalculateCacheDataV2DTO cacheDataDTO = priceCalculatorV2Service.calculate(settlementInfo);
        cacheDataDTO.setMemberId(0L);
        return cacheDataDTO;
    }

    @Override
    protected BzOrderDO buildBzOrderDO(KioskSubmitReqV2VO reqVO, CalculateCacheDataV2DTO cacheData) {
        BzOrderDO bzOrderDO = super.buildBzOrderDO(reqVO, cacheData);
        bzOrderDO.setPickUpNum(super.getPickUpCode(reqVO.getStoreId()));
        //现金单直接置为制作中
        bzOrderDO.setOrderState(OrderStateEnum.MAKING.getCode());
        bzOrderDO.setPayTime(LocalDateTime.now());

        return bzOrderDO;
    }

    @Override
    @Transactional
    public SubmitResVO submit(KioskSubmitReqV2VO reqVO) throws Exception {
        SubmitResVO result = super.submit(reqVO);

        // TODO 尽情发挥...

        return result;
    }

    @Override
    protected void postProcess(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheDataDTO, BzOrderDO bzOrderDO) {
        strongExecutor.submit(() -> {
            OrderDetailDTO detail = super.getOrderDetail(cacheDataDTO, bzOrderDO);

            try {
                log.info("==> 【点餐机快速下单】扣减原材料库存 {}", bzOrderDO.getOrderSn());
                bzOrderService.changeStock(bzOrderDO, detail, StockChangeEnum.SALE);
            } catch (Exception e) {
                log.error("==> 【点餐机快速下单】扣减原材料库存 失败 {}", bzOrderDO.getOrderSn(), e);
            }
        });

        weakExecutor.submit(() -> {

            try {
                log.info("==> 【点餐机快速下单】kafka发消息 {}", bzOrderDO.getOrderSn());
                bzOrderService.notifyOrder(bzOrderDO.getOrderSn(), "INSERT");
            } catch (Exception e) {
                log.error("==> 【点餐机快速下单】kafka发消息 失败 {}", bzOrderDO.getOrderSn(), e);
            }

            try {
                log.info("==> 【点餐机快速下单】缓存商品销量+ orderSn {}", cacheDataDTO.getOrderSn());
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
                log.error("==> 【点餐机快速下单】缓存商品销量+ 失败 orderSn {}", cacheDataDTO.getOrderSn(), e);
            }
        });
    }
}
