package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@TableName(value = "activity_jk_prize_exchange", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityJkPrizeExchangeDO extends BusinessBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(name = "activityId", description = "活动主id")
    private Long activityId;

    @Schema(name = "prizeId", description = "奖品id")
    private Long prizeId;

    @Schema(name = "storeId", description = "门店id")
    private Long storeId;


    @Schema(name = "prizeState", description = "0 已发放   商品类型  1未填写收货地址  2 已填写地址 待发货  3已发货  9 退回 ")
    private Integer prizeState;

    @Schema(name = "prizeType", description = "奖品类型 1 积分 2 优惠卷 3 优惠卷包  4 实物 5现金红包")
    private Integer prizeType;

    @Schema(name = "awardId", description = "奖品id(根据类型判断是优惠卷 id 还是商品 id)")
    private Long awardId;

    @Schema(name = "userCouponId", description = "用户所持有的优惠卷id")
    private Long userCouponId;

    /** 奖品价值 */
    @Schema(name = "prizeValue", description = "奖品价值")
    private BigDecimal prizeValue;

    /** 奖品名称 */
    @Schema(name = "prizeName", description = "奖品名称")
    private String prizeName;

    /** 奖品图片 */
    @Schema(name = "prizeImgUrl", description = "奖品图片")
    private String prizeImgUrl;

    /** 会员ID */
    @Schema(name = "memberId", description = "会员ID")
    private Long memberId;
    /** 会员名称 */
    @Schema(name = "memberName", description = "会员名称")
    private String memberName;

    /** 快递单号 */
    @Schema(name = "trackingNumber", description = "快递单号")
    private String trackingNumber;

    /** 会员手机号 */
    @Schema(name = "memberMobile", description = "会员手机号")
    private String memberMobile;

    /** 快递公司 */
    @Schema(name = "expressCompany", description = "快递公司")
    private String expressCompany;

    /** 收货地址 */
    @Schema(name = "receiveAddress", description = "收货地址")
    private String receiveAddress;

    /** 收件人 */
    @Schema(name = "receiveUser", description = "收件人")
    private String receiveUser;

    /** 联系电话 */
    @Schema(name = "receiveMobile", description = "联系电话")
    private String receiveMobile;


    @Schema(name = "storeId", description = "商户转账单号")
    private String outBillNo;

    @Schema(name = "claimStatus", description = "红包领取状态(1 未领取  2 已领取  3已过期)")
    private Integer claimStatus;

    @Schema(name = "微信红包返参链接", description = "微信红包返参链接")
    private String packageInfo;

    @Schema(name = "storeName", description = "门店名称")
    @TableField(exist = false)
    private String storeName;

}
