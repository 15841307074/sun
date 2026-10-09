package com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 优惠券
 * </p>
 *
 */
@Data
@TableName("user_coupon")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponDO extends BusinessBaseDO {

    @Schema(description = "用户优惠卷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 优惠券编码
     */
    @Schema(description = "优惠券编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponCode;

    /**
     * 优惠券名称
     */
    @Schema(description = "优惠券名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponName;

    /**
     * 优惠券类型(0-满减券,1-直减券,2-折扣券)
     */
    @Schema(description = "优惠券类型(0-满减券,1-直减券,2-折扣券)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer couponType;

    /**
     * 优惠券剩余数目
     */
    @Schema(description = "优惠券剩余数目", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer couponNum;

    /**
     * 兑换券是否需要绑定商品 0否 1是
     */
    private Integer exchangeFlag;

    /**
     * 每人限领数目
     */
    @Schema(description = "每人限领数目", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitNum;

    /**
     * 指定门店id
     */
    @Schema(description = "指定门店id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    /**
     * 优惠券有效开始时间
     */
    @Schema(description = "优惠券有效开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date couponStartTime;

    /**
     * 优惠券有效结束时间
     */
    @Schema(description = "优惠券有效结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date couponEndTime;

    /**
     * 已领取数目
     */
    @Schema(description = "已领取数目", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer receivedNum;

    /**
     * 已使用数目
     */
    @Schema(description = "已使用数目", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer usedNum;

    /**
     * 单品id
     */
    @Schema(description = "单品id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String singleIds;

    /**
     * 满减金额
     */
    @Schema(description = "满减金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer fullReduction;

    /**
     * 减少金额
     */
    @Schema(description = "n件n元/折中的元/折", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal reduceAmount;

    /**
     * 折扣
     */
    @Schema(description = "n件n元/折中的件  也是折扣的折", requiredMode = Schema.RequiredMode.REQUIRED)
    private String discount;

    /**
     * 优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)
     */
    @Schema(description = "优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer couponStatus;

    /**
     * 是否上架(0-否,1-是)
     */
    @Schema(description = "是否上架(0-否,1-是)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isGround;

    /**
     * 领取方式(0-自动发放,1-手动领取)
     */
    @Schema(description = "领取方式(0-自动发放,1-手动领取)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long distributionMethod;

    /**
     * 优惠券说明
     */
    @Schema(description = "优惠券说明", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponExplain;

    /**
     * 使用规则
     */
    @Schema(description = "使用规则", requiredMode = Schema.RequiredMode.REQUIRED)
    private String useRules;

    /**
     * 优惠券图片
     */
    @Schema(description = "优惠券图片", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponImageUrl;

    /**
     * 持续时间
     */
    @Schema(description = "持续时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime couponCreateTime;

    /**
     * 使用类型 0 时间段 1 立即生效 2 领取N天后生效
     */
    @Schema(description = "使用类型 0 时间段 1 立即生效 2 领取N天后生效", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer useType;

    /**
     * 使用时间信息，根据use_type而定
     */
    @Schema(description = "使用时间信息，根据use_type而定", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponUseTime;

    /**
     * 备注
     */
    @Schema(description = "备注", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remark;

    /**
     * 适用门店范围 1:通用 2:门店券
     */
    @Schema(description = "适用门店范围 1:通用 2:门店券", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isCommon;

    /**
     * 指定组织ids
     */
    @Schema(description = "指定组织ids", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deptIds;

    /**
     * 领取人限制 （0 不限制 1 新注册用户 2 老用户）
     */
    @Schema(description = "领取人限制 （0 不限制 1 新注册用户 2 老用户）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userRestrictions;

    /**
     * 分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友
     */
    @Schema(description = "分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isShare;

    /**
     * 使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）
     */
    @Schema(description = "使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer doorsillType;

    /**
     * 门槛金额/件数
     */
    @Schema(description = "门槛金额/件数", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal doorsill;

    /**
     * 适用商品范围 1 通用 2指定商品可用 3指定商品不可用
     */
    @Schema(description = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isCommonStore;

    /**
     * 减免/折扣
     */
    @Schema(description = "减免/折扣", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal reliefOrDiscount;

    /**
     * 支付金额
     */
    @Schema(description = "支付金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal payAmount;

    /**
     * 显示时间
     */
    @Schema(description = "显示时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String showTime;

    /**
     * 版本号(乐观锁)
     */
    @Schema(description = "版本号(乐观锁)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long version;

    /**
     * 发放总量
     */
    @Schema(description = "发放总量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalNum;

    /**
     * 用户id
     */
    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    /**
     * 是否使用(0-待使用,1-已使用)
     */
    @Schema(description = "是否使用(0-待使用,1-已使用)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isUsed;

    /**
     * 到期时间
     */
    @Schema(description = "到期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date expirationTime;

    /**
     * 有效开始日期
     */
    @Schema(description = "有效开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date vaildStartTime;

    /**
     * 使用时间
     */
    @Schema(description = "使用时间", requiredMode = Schema.RequiredMode.REQUIRED)
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private String useTime;

    /**
     * 是否是新用户 0是 1不是
     */
    @Schema(description = "是否是新用户 0是 1不是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isNew;

    /**
     * 优惠券主键
     */
    @Schema(description = "优惠券主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long couponId;

    /**
     * 会员手机号
     */
    @Schema(description = "会员手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberMobile;
    /**
     * 会员名称
     */
    @Schema(description = "会员名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberName;


    /**
     * 优惠券来源 0 自领 1推广 2积分商城
     */
    @Schema(description = "优惠券来源 0 自领 1推广 2积分商城", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer couponSource;

    /**
     * 优惠券包id
     */
    @Schema(description = "优惠券包id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long packageId;

    /**
     * 优惠券剩余数目可见性(0-可见,1-不可见)
     */
    @Schema(description = "优惠券剩余数目可见性(0-可见,1-不可见)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer couponNumVisible;

    /**
     * 用餐方式 0 堂食 1 打包 2 外卖
     */
    @Schema(description = "用餐方式 0 堂食 1 打包 2 外卖", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer habit;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(exist = false)
    private transient String commodityNameStr;

    @TableField(exist = false)
    private String openId;

    @TableField(exist = false)
    private transient  String couponTypeName;

    /**
     * 使用说明
     */
    @Schema(description = "使用说明", requiredMode = Schema.RequiredMode.REQUIRED)
    private String instructions;
}
