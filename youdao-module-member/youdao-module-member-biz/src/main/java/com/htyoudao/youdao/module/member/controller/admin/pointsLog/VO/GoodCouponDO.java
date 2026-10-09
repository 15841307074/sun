package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券 DO
 *
 * @author dht
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GoodCouponDO extends BusinessBaseDO {

    /**
     * 主键
     */
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
    private String couponType;
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
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
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
     * 优惠券名字的颜色
     */
    private String couponNameColor;
    /**
     * 使用类型 0 时间段 1 立即生效 2 领取N天后生效
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
     * 适用门店范围 1:通用 2:门店券
     */
    private Integer isCommon;
    /**
     * 指定组织ids
     */
    private String deptIds;
    /**
     * 领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）
     */
    private Boolean userRestrictions;
    /**
     * 分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友
     */
    private Boolean isShare;
    /**
     * 使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）
     */
    private Boolean doorsillType;
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

}