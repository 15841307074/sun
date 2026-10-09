package com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 优惠券 DO
 *
 * @author dht
 */
@TableName("good_coupon")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GoodCouponDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 优惠券类型(0-满减券,1-折扣券)
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
     * 兑换券是否需要绑定商品 0否 1是
     */
    private Integer exchangeFlag;
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
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String singleIds;
    /**
     * 满减金额
     */
    private Integer fullReduction;
    /**
     * 减少金额
     */
    private BigDecimal reduceAmount;
    /**
     * 折扣
     */
    private String discount;
    /**
     * 名字拼接,0拼 1不拼
     */
    private Integer nameConcatenation;
    /**
     * 是否上架(0-否,1-是)
     */
    private Integer isGround;
    /**
     * 领取方式(0-自动发放,1-手动领取)
     */
    private Integer distributionMethod;
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
    private String couponNameColor;
    /**
     * 失效时间类型 0 时间段 1 立即生效 2 领取N天后生效 3 指定周几失效
     */
    private Integer useType;
    /**
     * 使用时间信息，根据use_type而定
     */
    private String useTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 适用门店范围 0:通用 1:门店券
     */
    private Integer isCommon;
    /**
     * 社群二维码
     */
    private String communityQrImage;
    /**
     * 领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）
     */
    private Integer userRestrictions;
    /**
     * 分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友
     */
    private Integer isShare;
    /**
     * 使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛  3免费领取商品 4付费领取商品）
     */
    private Integer doorsillType;
    /**
     * 门槛金额/件数，兑换券的支付金额
     */
    private BigDecimal doorsill;
    /**
     * 适用商品范围 1 通用 2指定商品可用 3指定商品不可用
     */
    private Integer isCommonStore;
    /**
     * 减免/折扣、兑换券的支付金额
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
     * 人群id
     */
    private String version;
    /**
     * 发放总量
     */
    private Integer totalNum;
    /**
     * 会员等级券 1 2 3 4 5
     */
    private Integer memberLevel;
    /**
     * 每日领取数量限制
     */
    private Integer dayLimit;
    /**
     * 优惠券剩余数目可见性(0-可见,1-不可见)
     */
    private Integer couponNumVisible;
    /**
     * 用餐方式 0 全部可用 1堂食可用 2外卖可用
     */
    private Integer habit;
    /**
     * 指定日期逗号分割
     */
    private String dayNumbers;
    /**
     * 指定周几逗号分割
     */
    private String weekNumbers;
    /**
     * 指定时间段
     */
    private String timeRange;
    /**
     * 是否全天时段
     */
    private Integer isAllDay;
    /**
     * 优惠券背景图片
     */
    private String couponBgImageUrl;
    /**
     * 微信分享短连接
     */
    private String miniSortUrl;
    /**
     * H5分享短连接
     */
    private String h5SortUrl;

    /**
     * 领取时间限制 0 不限制 1 限制时间
     */
    private Integer claimTimeLimit;

    /**
     * 领取时间 指定日期段 年月日#年月日
     */
    private String claimTimeSlot;

    /**
     * 领取时间指定几号 1#2#6
     */
    private String claimDayNo;

    /**
     * 领取时间指定周几 1#3
     */
    private String claimWeekNo;

    /**
     * 领取时间指定时间 时分秒#时分秒
     */
    private String claimTime;

    /**
     * 门店领取限制 0 不限制
     */
    private Integer storeLimitNum;

    /**
     * 绑定门店标签 0 绑定 1 不绑定
     */
    private Integer storeTagFlag;

    /**
     * 失效时间 纯前端用
     */
    private Integer testAfterDate;

    /**
     * 0:不需要进社群 1:需要进社群
     */
    private Integer communityFlag;
}