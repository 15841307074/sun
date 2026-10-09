package com.htyoudao.youdao.module.member.api.point.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.member.api.pointsproduct.dto.PointsProductDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author dht
 */
@Data
public class PointsLogDTO implements Serializable {

    private Long pointsLogId;

    /** 会员ID */
    private Long memberId;

    private String memberName;

    /** 手机号 */
    private String memberMobile;

    /** 商品名称 */
    private String productName;

    /** 订单编号 */
    private String orderSn;

    /** 订单总价 */
    private BigDecimal orderAmount;
    /**
     * 积分记录状态 1 正常 2 过期
     */
    private Integer pointsLogStatus;


    /** 积分商品ID */
    private Long productId;

    /** 积分商品价格 */
    private Long productPrice;

    private String logCode;

    /** 积分变更价格 */
    private Long pointsChange;
    /** 快递单号 */
    private String trackingNumber;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 是否是积分商品
     */
    private Integer isPointsProduct;

    /** 会员昵称 */

    private String memberNickName;

    /**
     * 收货地址
     */
    private String receiveAddress;

    /** 商品类型 1 优惠劵 2 实体积分商品 */
    private Integer productType;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd hh:MM:ss")
    private Date expirationTime;

    /**
     * 积分类型(1-获取,2-兑换商品消耗,3-过期,4-冻结)
     */
    private Integer pointsType;
    /**
     * 分表字段 电话后两位取模
     */
    private Integer shardingValue;

    private PointsProductDTO pointsProduct;
}
