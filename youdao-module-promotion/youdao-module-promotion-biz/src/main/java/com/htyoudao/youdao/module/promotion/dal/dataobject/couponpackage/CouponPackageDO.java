package com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 优惠券包 DO
 *
 * @author dht
 */
@TableName("coupon_package")
@KeySequence("coupon_package_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponPackageDO extends BusinessBaseDO {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 优惠券包名
     */
    private String packageName;
    /**
     * 发放总量
     */
    private Integer totalNum;
    /**
     * 备注
     */
    private String remark;
    /**
     * 分享设置  0 不允许转发 1 允许转发给好友 2 允许复制链接分享给好友
     */
    private Integer isShare;
    /**
     * 剩余数量
     */
    private Integer packageNum;
    /**
     * 已领取数目
     */
    private Integer receivedNum;
    /**
     * 每日领取限制
     */
    private Integer dayLimit;
    /**
     * 显示时间
     */
    private String showTime;
    /**
     * 每人限领数目
     */
    private Integer limitNum;
    /**
     * 领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）
     */
    private Integer userRestrictions;

    /**
     * 人群id
     */
    private String crowdId;

    /**
     * 优惠券图片
     */
    private String packageImageUrl;
    /**
     * 是否上架(0-否,1-是)
     */
    private Integer isGround;
    /**
     * 微信分享短连接
     */
    private String miniSortUrl;
    /**
     * H5分享短连接
     */
    private String h5SortUrl;

    /**
     * 门店领取限制 0 不限制
     */
    private Integer storeLimitNum;

    @TableField(exist = false)
    private Long storeId;

    /**
     * 0 普通券包 1 周周惠券包
     */
    private Integer packageType;

    /**
     * 发放的会员等级 12345
     */
    private Integer memberLevel;

    /**
     * 是否需要进社群 0:不需要进社群 1:需要进社群
     */
    private Integer communityFlag;


    /**
     * 社群二维码
     */
    private String communityQrImage;

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

}