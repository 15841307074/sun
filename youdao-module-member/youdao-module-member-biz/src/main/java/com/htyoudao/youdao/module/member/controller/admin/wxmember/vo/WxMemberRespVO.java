package com.htyoudao.youdao.module.member.controller.admin.wxmember.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
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
public class WxMemberRespVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会员id
     */
    @Schema(description = "会员id")
    private Long memberId;

    /**
     * 用户名（登录名称）
     */
    @Schema(description = "用户名（登录名称）")
    private String memberName;

    /**
     * 会员昵称
     */
    @Schema(description = "会员昵称")
    private String memberNickName;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String memberMobile;

    /**
     * 会员标签值
     */
    @Schema(description = "会员标签值")
    private String memberTags;

    /**
     * 会员可用积分
     */
    @Schema(description = "会员可用积分")
    private Integer memberIntegral;

    /**
     * 总积分数量
     */
    @Schema(description = "总积分数量")
    private Long integralFrozen;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String memberAvatar;

    /**
     * 性别：0、保密；1、男；2、女
     */
    @Schema(description = "性别：0、保密；1、男；2、女")
    private Integer gender;

    /**
     * 注册时间
     */
    @Schema(description = "注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date registerTime;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    /**
     * 登录次数
     */
    @Schema(description = "登录次数")
    private Integer loginNumber;

    /**
     * 会员来源 0普通下单
     */
    @Schema(description = "会员来源 0普通下单")
    private Byte memberChannel;

    /**
     * 用户身份 0非会员 1 会员
     */
    @Schema(description = "用户身份 0非会员 1 会员")
    private Integer userIdentity;

    /**
     * 会员状态：0-禁用，1-启用；默认为1
     */
    @Schema(description = "会员状态：0-禁用，1-启用；默认为1")
    private Integer state;

    /**
     * 微信用户统一标识
     */
    @Schema(description = "微信用户统一标识")
    private String wxUnionid;

    /**
     * 微信用户标识
     */
    @Schema(description = "微信用户标识")
    private String openid;

    /**
     * 微信用户头像
     */
    @Schema(description = "微信用户头像")
    private String wxAvatarImg;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 会员等级
     */
    @Schema(description = "会员等级")
    private Integer grade;

    /**
     * 生日
     */
    @Schema(description = "生日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    private Date memberBirthday;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date finalTime;

    /**
     * 最后完成订单时间
     */
    @Schema(description = "最后完成订单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date finalOrderFinishTime;

    /**
     * 用户类别 0 微信 1支付宝
     */
    @Schema(description = "用户类别 0 微信 1支付宝")
    private Integer memberCategory;

    /**
     * 分表字段 电话后两位取模
     */
    @Schema(description = "分表字段 电话后两位取模")
    private Integer shardingValue;

    @Schema(description = "projectOwnerShip")
    private Long projectOwnerShip;

    /**
     * 冻结积分数量
     */
    @Schema(description = "冻结积分数量")
    private Long freezePoints;

    /**
     * 即将过期积分
     */
    @Schema(description = "即将过期积分")
    private Long overduePoints;

    /**
     * 是否是首次登录 0是 1不是
     */
    @Schema(description = "是否是首次登录 0是 1不是")
    private Integer isFirstLogin;
    /**
     * 会员等级
     */
    @Schema(description = "会员等级")
    private Integer memberLevel;


    /**
     * 历史订单数
     */

    @Schema(description = "历史订单数")
    private Integer totalOrderNum;

    /**
     * 历史订单数
     */
    @Schema(description = "历史订单数")
    private Integer memberLabel;

    /**
     * 会员状态
     */
    @Schema(description = "会员状态 0正常 1冻结 2待注销 3已注销")
    private Integer memberStatus;

    /**
     * 首次下单店铺id
     */
    @Schema(description = "首次下单店铺id")
    private Long firstOrderStoreId;

    /**
     * 首次下单店铺的组织
     */
    @Schema(description = "首次下单店铺的组织")
    private String orgName;

    /**
     * 首次下单店铺名称
     */
    @Schema(description = "首次下单店铺名称")
    private String firstOrderStoreName;

    /**
     * 倒数第二次下单时间
     */
    @Schema(description = "倒数第二次下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date secondOrderFinishTime;

    /**
     * 倒数第三次下单时间
     */
    @Schema(description = "倒数第三次下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date thirdOrderFinishTime;

    /**
     * 倒数第四次下单时间
     */
    @Schema(description = "倒数第四次下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date fourthOrderFinishTime;

    /**
     * 倒数第二次登录时间
     */
    @Schema(description = "倒数第二次登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date secondLoginTime;

    /**
     * 倒数第三次登录时间
     */
    @Schema(description = "倒数第三次登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date thirdLoginTime;

    /**
     * 倒数第四次登录时间
     */
    @Schema(description = "倒数第四次登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date fourthLoginTime;

    /**
     * 7天订单数
     */
    @Schema(description = "7天订单数")
    private Integer sevenDayOrderCount;

    /**
     * 7天单均付款
     */
    @Schema(description = "7天单均付款")
    private BigDecimal sevenDayOrderAvg;

    /**
     * 7天付款总数
     */
    @Schema(description = "7天付款总数")
    private BigDecimal sevenDayOrderSum;

    /**
     * 30天订单数
     */
    @Schema(description = "30天订单数")
    private Integer thirtyDayOrderCount;

    /**
     * 30天单均付款
     */
    @Schema(description = "30天单均付款")
    private BigDecimal thirtyDayOrderAvg;

    /**
     * 30天付款总数
     */
    @Schema(description = "30天付款总数")
    private BigDecimal thirtyDayOrderSum;

    /**
     * 半年订单数
     */
    @Schema(description = "半年订单数")
    private Integer halfYearOrderCount;

    /**
     * 半年单均付款
     */
    @Schema(description = "半年单均付款")
    private BigDecimal halfYearOrderAvg;

    /**
     * 半年付款总数
     */
    @Schema(description = "半年付款总数")
    private BigDecimal halfYearOrderSum;

    /**
     * 一年订单数
     */
    @Schema(description = "一年订单数")
    private Integer oneYearOrderCount;

    /**
     * 一年单均付款
     */
    @Schema(description = "一年单均付款")
    private BigDecimal oneYearOrderAvg;

    /**
     * 一年付款总数
     */
    @Schema(description = "一年付款总数")
    private BigDecimal oneYearOrderSum;

    @Schema(description = "绑定次数")
    private Integer bindingCount;

    @Schema(description = "用户换绑上限")
    private Integer bindingMax;

    @Schema(description = "2在社群 1不在社群", example = "0")
    private Integer communityFlag;

    @Schema(description = "是否是配送员 0不是 1是")
    private Integer errandFlag;

    @Schema(description = "可用余额")
    private BigDecimal balance;
}
