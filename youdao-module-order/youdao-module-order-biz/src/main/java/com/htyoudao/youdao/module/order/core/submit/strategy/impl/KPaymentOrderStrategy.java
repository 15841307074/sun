package com.htyoudao.youdao.module.order.core.submit.strategy.impl;

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
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_SETTLEMENT_INFO_NOT_NULL;

/**
 * <p>
 * 付款单，包括点餐机白盒
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Component
public class KPaymentOrderStrategy extends AbstractIOrderSubmitStrategy<KioskSubmitReqV2VO> {

    @Resource
    private PriceCalculatorV2Service priceCalculatorV2Service ;

    @Override
    public OrderSourceEnum getSource() {
        return OrderSourceEnum.K_PAYMENT_ORDER;
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
    protected String getPickUpCode(Long storeId) {
        return super.getPickUpCode(storeId);
    }

    @Override
    @Transactional
    public SubmitResVO submit(KioskSubmitReqV2VO reqVO) throws Exception {
        SubmitResVO result = super.submit(reqVO);

        // TODO 尽情发挥...
        return result;
    }

    @Override
    protected void postProcess(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheDataDTO, BzOrderDO orderDO) {
        super.postProcess(reqVO, cacheDataDTO, orderDO);
    }
}
