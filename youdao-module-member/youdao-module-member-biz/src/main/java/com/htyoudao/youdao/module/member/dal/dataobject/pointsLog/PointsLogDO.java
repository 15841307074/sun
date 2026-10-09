package com.htyoudao.youdao.module.member.dal.dataobject.pointsLog;


import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 积分记录对象 points_log
 *
 */
@Data
@TableName(value = "points_log")
public class PointsLogDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "记录ID")
    @TableId(value = "points_log_id")
    private Long pointsLogId;

    /** 会员ID */
    @Schema(description = "会员id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    /** 手机号 */
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberMobile;

    /** 商品名称 */
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    /** 订单编号 */
    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderSn;

    /** 订单总价 */
    @Schema(description = "订单总价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal orderAmount;
    /**
     * 积分记录状态 1 正常 2 过期
     */
    @Schema(description = "积分记录状态 1 正常 2 过期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pointsLogStatus;


    /** 积分商品ID */
    @Schema(description = "积分商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    /** 积分商品价格 */
    @Schema(description = "积分商品价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productPrice;

    @Schema(description = "日志code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logCode;

    /** 积分变更价格 */
    @Schema(description = "积分变更价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pointsChange;
    /** 快递单号 */
    @Schema(description = "快递单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String trackingNumber;

    /**
     * 快递公司
     */
    @Schema(description = "快递公司", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressCompany;

    /**
     * 是否是积分商品
     */
    @Schema(description = "是否是积分商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isPointsProduct;

    @Schema(description = "积分商品", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(exist = false)
    PointsProductDO pointsProductDO;


    /** 用户名（登录名称） */

    @Schema(description = "用户名（登录名称）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberName;

    /** 会员昵称 */
    @Schema(description = "会员昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberNickName;

    /**
     * 收货地址
     */
    @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiveAddress;

    /** 商品类型 1 优惠劵 2 实体积分商品 */
    @Schema(description = "商品类型 1 优惠劵 2 实体积分商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productType;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd hh:MM:ss")
    private Date expirationTime;

    /**
     * 积分类型(1-获取,2-兑换商品消耗,3-过期,4-冻结)
     */
    @Schema(description = "积分类型(1-获取,2-兑换商品消耗,3-过期,4-冻结)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pointsType;
    /**
     * 分表字段 电话后两位取模
     */
    @Schema(description = "分表字段 电话后两位取模", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer shardingValue;


}
