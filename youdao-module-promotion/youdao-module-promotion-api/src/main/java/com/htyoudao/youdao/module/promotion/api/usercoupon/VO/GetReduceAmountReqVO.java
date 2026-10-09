package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;

import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.CalculateCacheDataCopyDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: dht
 */
@Data
public class GetReduceAmountReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5872705758534423894L;
    /**
     * 指定门店id
     */
    private Long storeId;

    /**
     * 下单时商品入参
     */
    private List<OrderGoods> goodsList;

    /**
     * 领取方式
     */
    private Long distributionMethod;

    /**
     * userId
     */
    private Long userId;

    /**
     * 交易金额
     */
    private BigDecimal transactionAmount;

    /**
     * 用餐方式 0 堂食 1 打包 2 外卖
     */
    private Integer habit;

    /**
     * 指定优惠券id
     */
    private Long userCouponId;

    @Data
    public static class OrderGoods implements Serializable{

        @Serial
        private static final long serialVersionUID = 8074372004709925980L;
        /**
         * 商品原始id
         */
        private Long skuId;

        /**
         * 金额
         */
        private BigDecimal transactionAmount;

        /**
         * 商品id
         */
        private Long commodityId;
    }

    /**
     * 商品信息集合
     */
    private List<CalculateCacheDataCopyDTO.CommodityInfoVO> commodityInfos;
}
