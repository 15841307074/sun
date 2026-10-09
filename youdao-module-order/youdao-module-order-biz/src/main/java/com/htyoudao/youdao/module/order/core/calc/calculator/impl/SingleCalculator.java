package com.htyoudao.youdao.module.order.core.calc.calculator.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.commodity.api.DTO.CondimentInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.SettlementReqVO;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.core.calc.DTO.CostInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.calculator.PriceCalculator;
import com.htyoudao.youdao.module.order.core.calc.context.CalculatorContext;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 单品策略
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-28
 */
@Deprecated
public class SingleCalculator implements PriceCalculator {

    @Override
    public CostInfoDTO calculate(CalculatorContext context) {
//        //获取基础数据
//        SettlementReqVO.CommodityInfoVO commodity = context.commodity;
//        Map<Long, StoreSkuInfoDTO> skuMap = context.data.skus();
//
//        //获取SKU信息
//        StoreSkuInfoDTO sku = Optional.ofNullable(skuMap.get(commodity.getSkuId()))
//                .orElseThrow(() -> exception(ORDER_COMMODITY_NOT_EXISTS));
//
//        //商品校验
//        this.validateSku(commodity, sku, context.isDc);
//
//        CalculateCacheDataDTO.CommodityInfoVO cacheVO = new CalculateCacheDataDTO.CommodityInfoVO();
//        //拷贝基础属性
//        BeanUtils.copyProperties(commodity, cacheVO);
//        BeanUtils.copyProperties(sku, cacheVO);
//        cacheVO.setStackableActivities(sku.getStackableActivitieList());
//        cacheVO.setSetmealType(commodity.getSetmealType());
//        cacheVO.setIsPurchase(commodity.getIsPurchase());
//
//        //处理属性
//        List<SettlementReqVO.FlavorInfoVO> flavorList = commodity.getFlavorList();
//        List<CalculateCacheDataDTO.FlavorInfoVO> flavorInfoVOS = BeanCopyUtils.copyBeanList(flavorList, CalculateCacheDataDTO.FlavorInfoVO.class);
//        cacheVO.setFlavorList(flavorInfoVOS);
//        cacheVO.setImageUrl(
//                this.getFirstCommaElement(sku.getImageUrl())
//        );
//
//        //基础价格
//        BigDecimal basePrice = sku.getSkuPrice();
//
//        //小料价格
//        BigDecimal condimentsPrice = this.processCondiments(commodity, sku, cacheVO);
//
//        //单价 包含小料
//        cacheVO.setSkuPrice(basePrice.add(condimentsPrice));
//
//        //聚合价格
//        cacheVO.setGoodsShowPrice(basePrice.add(condimentsPrice).multiply(BigDecimal.valueOf(commodity.getCopies())).setScale(2, RoundingMode.HALF_UP));
//
//        //计算优惠价格
//        CalculatorResDTO calculatorResDTO = this.getPromotionDiscountAmount(context.data.activitys().get(commodity.getCommodityId()), cacheVO);
//
//        //计算包装费
//        BigDecimal packageFee = this.calculatePackagingFee(calculatorResDTO.getShouldPayPackageFeeCount(), sku.getManyCopy(), sku.getPackageFee());
//
//        //总价 = (基础价格 + 小料价格) * 份数
//        BigDecimal commodityAmount = basePrice.add(condimentsPrice)
//                .multiply(BigDecimal.valueOf(commodity.getCopies()));
//
//        return CostInfoDTO.builder()
//                .commodityAmount(commodityAmount)
//                .promotionDiscountAmount(calculatorResDTO.getPromotionDiscountAmount())
//                .packingFee(packageFee)
//                .activityDiscountAmount(BigDecimal.ZERO)
//                .deliveryFee(BigDecimal.ZERO)
//                .afterAmount(BigDecimal.ZERO)
//                .cacheVOList(calculatorResDTO.getCacheVOList())
//                .build();
        return null;
    }

    /**
     * 处理小料
     *
     * @param commodity 商品信息
     * @param sku       SKU信息
     * @param cacheVO   商品
     */
    private BigDecimal processCondiments(SettlementReqVO.CommodityInfoVO commodity, StoreSkuInfoDTO sku,
                                         CalculateCacheDataDTO.CommodityInfoVO cacheVO) {
        List<CalculateCacheDataDTO.CondimentInfoVO> condimentVOs = new ArrayList<>();
        BigDecimal totalCondimentsPrice = BigDecimal.ZERO;

        //处理小料逻辑
        if (CollectionUtils.isNotEmpty(commodity.getCondimentsList())) {
            List<CondimentInfoDTO> condiments = this.parseCondiments(sku);
            Map<Long, CondimentInfoDTO> condimentMap = condiments.stream()
                    .collect(Collectors.toMap(CondimentInfoDTO::getCondimentId, Function.identity(), (v1, v2) -> v1));

            //小料是否多选
            if (ObjectUtils.isNotEmpty(sku.getCondimentIsMore()) && sku.getCondimentIsMore() == 0 && commodity.getCondimentsList().size() > 1) {
                throw new ServiceException(new ErrorCode(ORDER_COMMODITY_CONDIMENT_COPIES_ERROR.getCode(), String.format(ORDER_COMMODITY_CONDIMENT_COPIES_ERROR.getMsg(), sku.getSpuName())));
            }

            int condimentTotalNumber = 0;
            for (SettlementReqVO.CondimentInfoVO condimentReq : commodity.getCondimentsList()) {
                condimentTotalNumber += condimentReq.getNumber();

                CondimentInfoDTO condiment = Optional.ofNullable(condimentMap.get(condimentReq.getId()))
                        //小料ID校验
                        .orElseThrow(() -> new ServiceException(new ErrorCode(ORDER_COMMODITY_CONDIMENT_ID_ERROR.getCode(), String.format(ORDER_COMMODITY_CONDIMENT_ID_ERROR.getMsg(), condimentReq.getId()))));

                //小料上下架
                if (OrderConstants.NO.equals(condiment.getStatus())) {
                    throw new ServiceException(new ErrorCode(ORDER_COMMODITY_CONDIMENT_DOWN.getCode(), String.format(ORDER_COMMODITY_CONDIMENT_DOWN.getMsg(), condiment.getCondimentName())));
                }

                CalculateCacheDataDTO.CondimentInfoVO condimentVO = new CalculateCacheDataDTO.CondimentInfoVO();
                BeanUtils.copyProperties(condiment, condimentVO);
                condimentVO.setId(condiment.getCondimentId());
                condimentVO.setName(condiment.getCondimentName());
                condimentVO.setNumber(condimentReq.getNumber());
                condimentVOs.add(condimentVO);

                //小料价格 = 单价 * 数量 * 份数
                BigDecimal condimentPrice = condiment.getPrice().multiply(BigDecimal.valueOf(condimentReq.getNumber()));
                totalCondimentsPrice = totalCondimentsPrice.add(condimentPrice);
            }

            //小料数量上限校验
            if (ObjectUtils.isNotEmpty(sku.getMaxCondimentNumber()) && sku.getMaxCondimentNumber() > 0 && condimentTotalNumber > sku.getMaxCondimentNumber()) {
                throw new ServiceException(new ErrorCode(ORDER_COMMODITY_CONDIMENT_UNMBER_ERROR.getCode(), String.format(ORDER_COMMODITY_CONDIMENT_UNMBER_ERROR.getMsg(), sku.getSpuName())));
            }
        }

        cacheVO.setCondimentsList(condimentVOs);
        return totalCondimentsPrice;
    }

    /**
     * 解析小料
     *
     * @param sku
     * @return
     */
    private List<CondimentInfoDTO> parseCondiments(StoreSkuInfoDTO sku) {
        try {
            return JSON.parseArray(sku.getCondiments(), CondimentInfoDTO.class);
        } catch (Exception e) {
            throw new ServiceException(new ErrorCode(ORDER_COMMODITY_CONDIMENT_PARSE_ERROR.getCode(), String.format(ORDER_COMMODITY_CONDIMENT_PARSE_ERROR.getMsg(), sku.getSpuName())));
        }
    }
}
