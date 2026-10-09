package com.htyoudao.youdao.module.order.core.calc.calculator;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.github.yulichang.toolkit.SpringContentUtils;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.commodity.enums.SaleRuleEnum;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.SettlementReqVO;
import com.htyoudao.youdao.module.order.core.activity.DTO.CalculatorResDTO;
import com.htyoudao.youdao.module.order.core.activity.factory.ActivityStrategyFactory;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.core.calc.DTO.CostInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.context.CalculatorContext;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityBaseDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_COMMODITY_SALE_RULE_ERROR;

/**
 * <p>
 * 价格计算器
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-28
 */
public interface PriceCalculator {

    ActivityStrategyFactory activityStrategyFactory = SpringContentUtils.getBean(ActivityStrategyFactory.class);

    CostInfoDTO calculate(CalculatorContext context);

    /**
     * 计算商品包装费 (BigDecimal 精确版本)
     *
     * @param copies     购买的商品数量
     * @param manyCopy   每多少份收取包装费（例如：每2个收取一次）
     * @param packageFee 每次收取的包装费金额（使用 BigDecimal 保证精度）
     * @return 总包装费用
     */
    default BigDecimal calculatePackagingFee(int copies, int manyCopy, BigDecimal packageFee) {
        // 参数校验
        if (copies <= 0 || manyCopy <= 0 || packageFee == null || packageFee.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 转换为 BigDecimal 进行计算
        BigDecimal quantityBD = new BigDecimal(copies);
        BigDecimal packSizeBD = new BigDecimal(manyCopy);

        // 计算包装单位数量（向上取整）
        BigDecimal oneFee = packageFee.divide(packSizeBD, 2, RoundingMode.HALF_UP);

        // 计算总费用（保留两位小数）
        return oneFee.multiply(quantityBD).setScale(2, RoundingMode.HALF_UP);
    }

    default CalculatorResDTO getPromotionDiscountAmount(List<? extends ActivityBaseDTO> activityList, CalculateCacheDataDTO.CommodityInfoVO cacheVO) {
        if (CollectionUtils.isEmpty(activityList)) {
            BigDecimal strikeThroughPrice = Optional.ofNullable(cacheVO.getStrikeThroughPrice()).orElse(BigDecimal.ZERO);
            cacheVO.setStrikeThroughPrice(strikeThroughPrice.multiply(BigDecimal.valueOf(cacheVO.getCopies())));

            return this.buildDefaultCalculatorResDTO(cacheVO);
        }

        List<CalculatorResDTO> calculateResList = new ArrayList<>();
        for (ActivityBaseDTO activity : activityList) {
            CalculatorResDTO calculatorResDTO = activityStrategyFactory.getStrategy(activity.getActivityType()).calculate(cacheVO, activity);
            if (calculatorResDTO != null) {
                calculateResList.add(calculatorResDTO);
            }
        }

        // 查找最大优惠
        CalculatorResDTO maxDiscountCalculatorRes = calculateResList.stream()
                .filter(res -> res.getPromotionDiscountAmount() != null
                        && res.getPromotionDiscountAmount().compareTo(BigDecimal.ZERO) >= 0)
                .max(
                        Comparator.comparing(CalculatorResDTO::getPromotionDiscountAmount)
                                .thenComparing(CalculatorResDTO::getCreateTime, Comparator.naturalOrder())
                )
                .orElse(this.buildEmptyCalculatorResDTO());

        if (CollectionUtils.isEmpty(maxDiscountCalculatorRes.getCacheVOList())) {
            BigDecimal strikeThroughPrice = Optional.ofNullable(cacheVO.getStrikeThroughPrice()).orElse(BigDecimal.ZERO);
            cacheVO.setStrikeThroughPrice(strikeThroughPrice.multiply(BigDecimal.valueOf(cacheVO.getCopies())));
            maxDiscountCalculatorRes.getCacheVOList().add(cacheVO);
            maxDiscountCalculatorRes.setShouldPayPackageFeeCount(cacheVO.getCopies());
        }

        return maxDiscountCalculatorRes;
    }

    //无活动时的返回
    private CalculatorResDTO buildDefaultCalculatorResDTO(CalculateCacheDataDTO.CommodityInfoVO cacheVO) {
        return CalculatorResDTO.builder()
                .cacheVOList(Collections.singletonList(cacheVO))
                .shouldPayPackageFeeCount(cacheVO.getCopies())
                .promotionDiscountAmount(BigDecimal.ZERO)
                .build();
    }

    //用于 max 查询为空时
    private CalculatorResDTO buildEmptyCalculatorResDTO() {
        return CalculatorResDTO.builder()
                .cacheVOList(new ArrayList<>())
                .promotionDiscountAmount(BigDecimal.ZERO)
                .build();
    }

    default void validateSku(SettlementReqVO.CommodityInfoVO commodity, StoreSkuInfoDTO sku, Boolean isDc) {
        //连锁库ID和SKU必须匹配
        if (ObjectUtils.isEmpty(commodity.getCommodityId()) || !commodity.getCommodityId().equals(sku.getCommodityId())) {
            throw new ServiceException(new ErrorCode(ORDER_COMMODITY_ID_NOT_MATCH.getCode(), String.format(ORDER_COMMODITY_ID_NOT_MATCH.getMsg(), sku.getSpuName())));
        }

        //上下架校验
        if ((sku.getTimeSharingTopping() == 1 && !sku.getIsUp()) || (isDc && sku.getCommodityStoreSpuMachineStatus() == 0) || (!isDc && sku.getCommodityStoreSpuAppletStatus() == 0)) {
            throw new ServiceException(new ErrorCode(ORDER_COMMODITY_IS_NOT_UP.getCode(), String.format(ORDER_COMMODITY_IS_NOT_UP.getMsg(), sku.getSpuName())));
        }

        //起订校验
        if (commodity.getCopies() < sku.getLimitBuyNumber()) {
            throw new ServiceException(new ErrorCode(ORDER_COMMODITY_BUY_NUMBER_ERROR.getCode(), String.format(ORDER_COMMODITY_BUY_NUMBER_ERROR.getMsg(), sku.getSpuName(), sku.getLimitBuyNumber())));
        }

        //售卖规则校验
        if (SaleRuleEnum.SALE_OK.getCode() != sku.getSaleRule()) {
            throw new ServiceException(new ErrorCode(ORDER_COMMODITY_SALE_RULE_ERROR.getCode(), String.format(ORDER_COMMODITY_SALE_RULE_ERROR.getMsg(), sku.getSpuName(), SaleRuleEnum.getMessageByCode(sku.getSaleRule()))));
        }
    }

    /**
     * 获取逗号分隔字符串中的第一个元素。
     * 如果不包含逗号，则返回原字符串（去除前后空格）。
     *
     * @param input 输入字符串
     * @return 第一个元素
     */
    default String getFirstCommaElement(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        input = input.trim();
        if (input.contains(",")) {
            return input.split(",")[0].trim(); // 返回第一个元素并去掉前后空格
        }
        return input;
    }
}
