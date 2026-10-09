package com.htyoudao.youdao.module.member.api.wxmember.VO;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 会员
 * </p>
 *
 * @author duht
 * @since 2024-10-08 13:37
 */

@Data
public class WxMemberDataDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会员id
     */
    @TableId(value = "member_id", type = IdType.ASSIGN_ID)
    private Long memberId;

    /**
     * 用户名（登录名称）
     */
    private String memberName;

    /**
     * 会员昵称
     */
    private String memberNickName;

    /**
     * 手机号
     */
    private String memberMobile;

    /**
     * 会员可用积分
     */
    private Integer memberIntegral;

    /**
     * 总积分数量
     */
    private Long integralFrozen;

    /**
     * 用户头像
     */
    private String memberAvatar;

    /**
     * 性别：0、保密；1、男；2、女
     */
    private Integer gender;

    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date registerTime;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    /**
     * 登录次数
     */
    private Integer loginNumber;

    /**
     * 会员来源 0普通下单
     */
    private Integer memberChannel;

    /**
     * 用户身份 0非会员 1 会员
     */
    private Integer userIdentity;

    /**
     * 会员状态：0-禁用，1-启用；默认为1
     */
    private Integer state;

    /**
     * 微信用户统一标识
     */
    private String wxUnionid;

    /**
     * 微信用户标识
     */
    private String openid;

    /**
     * 微信用户头像
     */
    private String wxAvatarImg;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 会员等级
     */
    private Integer grade;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    private Date memberBirthday;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date finalTime;

    /**
     * 最后完成订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date finalOrderFinishTime;

    /**
     * 用户类别 0 微信 1支付宝
     */
    private Integer memberCategory;

    /**
     * 分表字段 电话后两位取模
     */
    private Integer shardingValue;

    private Long projectOwnerShip;

    /**
     * 冻结积分数量
     */
    private Long freezePoints;

    /**
     * 即将过期积分
     */
    private Long overduePoints;

    /**
     * 是否是首次登录 0是 1不是
     */
//    private Integer isFirstLogin;
    /**
     * 会员等级
     */
    private Integer memberLevel;


    /**
     * 历史订单数
     */
    @Schema(description = "历史订单数量")
    private Integer totalOrderNum;

    /**
     * 历史订单数
     */
    @Schema(description = "会员标签")
    private Integer memberLabel;

    /**
     * 会员状态
     */
    @Schema(description = "会员状态 0正常 1冻结 2待注销 3已注销")
    private Integer memberStatus;

    /**
     * 首次下单店铺id
     */
    private Long firstOrderStoreId;

    /**
     * 首次下单店铺名称
     */
//    private String firstOrderStoreName;

    /**
     * 倒数第二次下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date secondOrderFinishTime;

    /**
     * 倒数第三次下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date thirdOrderFinishTime;

    /**
     * 倒数第四次下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date fourthOrderFinishTime;

//    /**
//     * 倒数第五次下单时间
//     */
//    @ExcelIgnore
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    @JSONField(format="yyyy-MM-dd HH:mm:ss")
//    private Date fifthOrderFinishTime;

    /**
     * 倒数第二次登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date secondLoginTime;

    /**
     * 倒数第三次登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date thirdLoginTime;

    /**
     * 倒数第四次登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date fourthLoginTime;

    /**
     * 7天订单数
     */
    private Integer sevenDayOrderCount;

    /**
     * 7天单均付款
     */
    private BigDecimal sevenDayOrderAvg;

    /**
     * 7天付款总数
     */
    private BigDecimal sevenDayOrderSum;

    /**
     * 30天订单数
     */
    private Integer thirtyDayOrderCount;

    /**
     * 30天单均付款
     */
    private BigDecimal thirtyDayOrderAvg;

    /**
     * 30天付款总数
     */
    private BigDecimal thirtyDayOrderSum;

    /**
     * 半年订单数
     */
    private Integer halfYearOrderCount;

    /**
     * 半年单均付款
     */
    private BigDecimal halfYearOrderAvg;

    /**
     * 半年付款总数
     */
    private BigDecimal halfYearOrderSum;

    /**
     * 一年订单数
     */
    private Integer oneYearOrderCount;

    /**
     * 一年单均付款
     */
    private BigDecimal oneYearOrderAvg;

    /**
     * 一年付款总数
     */
    private BigDecimal oneYearOrderSum;

    /**
     * 项目 id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long businessId;
}
