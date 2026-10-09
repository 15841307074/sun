package com.htyoudao.youdao.module.order.core.calc.v2.VO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 结算
 * </p>
 *
 * @author zhangjihe
 * @since 2025-02-13
 */
@Data
public class SettlementReqV2VO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8982688514518248605L;

    @Schema(description = "拼单主体ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429696")
    private String mainId;

    @Schema(description = "订单类型 0堂食 1打包 2外卖 3代取", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429696")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    @Schema(description = "用户优惠券ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429696")
    private Long userCouponId;

    @Schema(description = "下单电话", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "18200029384")
    private String takeAwayTel;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "https://example.com/avatar.jpg")
    private String memberAvatar;

    @Schema(description = "用户支付跑腿赏金", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "3.00")
    private BigDecimal errandRewardAmount;

    @Schema(description = "秒杀信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "{}")
    private SeckillInfo seckillInfo;

    @Data
    public static class SeckillInfo {

        @Schema(description = "场次ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429696")
        @NotNull(message = "场次ID不能为空")
        private Integer sessionId;

        @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429691")
        @NotNull(message = "活动ID不能为空")
        private Long activityId;

        @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1278042588310429691")
        private String activityName;

        @Schema(description = "渠道")
        private String channel;
    }

    @Valid
    @Schema(description = "商品信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "[]")
    @Size(min = 1, message = "商品信息不能为空")
    private List<CommodityInfoVO> commodityInfos;

    @Valid
    @Data
    @Schema(description = "商品信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    public static class CommodityInfoVO {

        @Schema(description = "商品SPU ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429696")
        private Long spuId;

        @Schema(description = "门店SKU ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429691")
        private Long skuId;

        @Schema(description = "连锁库商品ID (原始的spuId)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429634")
        private Long commodityId;

        @Schema(description = "商品数量", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
        @NotNull(message = "商品数量不能为空")
        @Min(value = 1, message = "商品数量不能小于1份")
        private Integer copies;

        @Schema(description = "套餐类型 1.固定搭配套餐，2.分组可选套餐, 3单品", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "套餐类型不能为空")
        private Integer setmealType;

        @Schema(description = "是否为加购商品 1.是 0.否", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "是否为加购商品标识 不能为空")
        private Integer isPurchase;

        @Schema(description = "是否为秒杀商品", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Boolean isSeckill;

        @Schema(description = "加购商品ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1278042588310429691")
        private Long afterId;

        @Schema(description = "套餐子项集合，带属性，有套餐时必传", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private List<SingleFlavorVO> singleFlavorList = new ArrayList<>();

        @Schema(description = "商品属性集合", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "[]")
        private List<FlavorInfoVO> flavorList = new ArrayList<>();

        @Schema(description = "商品小料集合", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "[]")
        private List<CondimentInfoVO> condimentsList = new ArrayList<>();
    }

    @Data
    public static class CondimentInfoVO {

        @Schema(description = "小料ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1278042588310429691")
        private Long id;

        @Schema(description = "小料数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        private Integer number;
    }

    @Data
    public static class FlavorInfoVO {

        @Schema(description = "属性名", requiredMode = Schema.RequiredMode.REQUIRED, example = "辣度")
        private String name;

        @Schema(description = "属性值", requiredMode = Schema.RequiredMode.REQUIRED, example = "微辣")
        private String value;
    }

    @Data
    public static class SingleFlavorVO {
        private Long singleId;
        private Integer num;
        private List<FlavorInfoVO> flavors = new ArrayList<>();
    }

    @JsonIgnore
    private Boolean isDc = false;
}
