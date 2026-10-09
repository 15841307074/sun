package com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 优惠券
 * </p>
 *
 */
@Data
public class UserCouponDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 创建人
     */
    private String createUserName;

    /**
     * 创建时间
     */

    private Date couponCreateTime;

    /**
     * 修改人
     */
    private String updateUserName;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
//    @Column(isLogicDelete = true)
    private Integer isDelete;

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
     * 项目id
     */
    private Long projectId;

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
     * 领取时间
     */
    private Date createTime;

    /**
     * 到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
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
     * 优惠卷背景图
     */
    private String couponBgImageUrl;

    @Schema(name = "用餐方式 0 堂食 1 打包 2 外卖", description = "用餐方式 0 堂食 1 打包 2 外卖")
    private Integer habit;

    /**
     * 优惠券剩余数目可见性(0-可见,1-不可见)
     */
    private Integer couponNumVisible;

}
