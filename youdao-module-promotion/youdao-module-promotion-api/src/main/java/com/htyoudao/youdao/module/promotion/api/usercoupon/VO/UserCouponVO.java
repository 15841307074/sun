package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;



/**
 * <p>
 * 优惠券
 * </p>
 *
 * @author duht
 * @since 2024-11-20
 */
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponVO implements Serializable {

    private Long id;

    /**
     * 优惠券编码
     */
    private String couponCode;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券类型(0-满减券,1-直减券,2-折扣券)
     */
    private Integer couponType;

    /**
     * 优惠券剩余数目
     */
    private Integer couponNum;

    /**
     * 每人限领数目
     */
    private Integer limitNum;

    /**
     * 指定门店id
     */
    private String storeId;

    /**
     * 优惠券有效开始时间
     */
    private Date couponStartTime;

    /**
     * 优惠券有效结束时间
     */
    private Date couponEndTime;

    /**
     * 已领取数目
     */
    private Integer receivedNum;

    /**
     * 已使用数目
     */
    private Integer usedNum;

    /**
     * 单品id
     */
    private String singleIds;

    /**
     * 满减金额
     */
    private BigDecimal fullReduction;

    /**
     * 减少金额
     */
    private BigDecimal reduceAmount;

    /**
     * 折扣
     */
    private String discount;

    /**
     * 优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)
     */
    private Integer couponStatus;

    /**
     * 是否上架(0-否,1-是)
     */
    private Integer isGround;

    /**
     * 领取方式(0-自动发放,1-手动领取)
     */
    private Long distributionMethod;

    /**
     * 优惠券说明
     */
    private String couponExplain;

    /**
     * 使用规则
     */
    private String useRules;

    /**
     * 优惠券图片
     */
    private String couponImageUrl;

    /**
     * 持续时间
     */
    private Integer duration;

    /**
     * 创建时间
     */
    private LocalDateTime couponCreateTime;

    /**
     * 使用类型 0 时间段 1 立即生效 2 领取N天后生效
     */
    private Integer useType;

    /**
     * 使用时间信息，根据use_type而定
     */
    private String couponUseTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 适用门店范围 1:通用 2:门店券
     */
    private Integer isCommon;

    /**
     * 指定组织ids
     */
    private String deptIds;

    /**
     * 领取人限制 （0 不限制 1 新注册用户 2 老用户）
     */
    private Integer userRestrictions;

    /**
     * 分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友
     */
    private Integer isShare;

    /**
     * 使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）
     */
    private Integer doorsillType;

    /**
     * 门槛金额/件数
     */
    private BigDecimal doorsill;

    /**
     * 适用商品范围 1 通用 2指定商品可用 3指定商品不可用
     */
    private Integer isCommonStore;

    /**
     * 减免/折扣
     */
    private BigDecimal reliefOrDiscount;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 显示时间
     */
    private String showTime;

    /**
     * 版本号(乐观锁)
     */
    private Long version;

    /**
     * 发放总量
     */
    private Integer totalNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 是否使用(0-待使用,1-已使用)
     */
    private Integer isUsed;

    /**
     * 到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date expirationTime;

    /**
     * 有效开始日期
     */
    private Date vaildStartTime;

    /**
     * 使用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private String useTime;

    /**
     * 是否是新用户 0是 1不是
     */
    private Integer isNew;

    /**
     * 优惠券主键
     */
    private Long couponId;

    /**
     * 会员手机号
     */
    private String memberMobile;

    /**
     * 优惠券来源 0 自领 1推广 2积分商城
     */
    private Integer couponSource;

    /**
     * 优惠券包id
     */
    private Long packageId;

    /**
     * 优惠券剩余数目可见性(0-可见,1-不可见)
     */
    private Integer couponNumVisible;

    /**
     * 用餐方式 0 堂食 1 打包 2 外卖
     */
    private Integer habit;

    /**
     * 商品名称
     */

    private transient String commodityNameStr;


    private String openId;


    private transient  String couponTypeName;
}
