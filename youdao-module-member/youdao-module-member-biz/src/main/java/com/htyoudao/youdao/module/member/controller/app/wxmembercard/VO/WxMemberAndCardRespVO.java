package com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class WxMemberAndCardRespVO {

    /** 会员卡背景图 */
    @Schema(description = "会员卡背景图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String backgroundImage;

    /**
     * 会员卡缩略图
     */
    @Schema(description = "会员卡缩略图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String thumbnailImage;

    /** 用户头像 */
    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberAvatar;

    /** 会员昵称 */
    @Schema(description = "会员昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberNickName;

    /** 会员可用积分 */
    @Schema(description = "会员可用积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long allPoints;

    /** 冻结积分数量 */
    @Schema(description = "冻结积分数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long integralFrozen;

    /** 积分最高门槛 */
    @Schema(description = "积分最高门槛", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long maxPointsThreshold;

    /** 会员卡描述 */
    @Schema(description = "会员卡描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "会员等级", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer memberLevel;
    /**
     * 会员积分
     */
    @Schema(description = "会员积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer memberIntegral;

    /**
     * 会员id
     */
    @Schema(description = "会员id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    /**
     * 用户名（登录名称）
     */
    @Schema(description = "用户名（登录名称）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberName;


    /**
     * 手机号
     */
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberMobile;

    @Schema(description = "是否是顶级", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isFlag = 0;




    /**
     * 性别：0、保密；1、男；2、女
     */
    @Schema(description = "性别：0、保密；1、男；2、女", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gender;

    /**
     * 注册时间
     */
    @Schema(description = "注册时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date registerTime;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date lastLoginTime;

    /**
     * 登录次数
     */
    @Schema(description = "登录次数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer loginNumber;

    /**
     * 用户身份 0非会员 1 会员
     */
    @Schema(description = "用户身份 0非会员 1 会员", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberChannel;

    /**
     * 用户身份 0非会员 1 会员
     */
    @Schema(description = "用户身份 0非会员 1 会员", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userIdentity;

    /**
     * 会员状态：0-禁用，1-启用；默认为1
     */
    @Schema(description = "会员状态：0-禁用，1-启用；默认为1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer state;

    /**
     * 微信用户统一标识
     */
    @Schema(description = "微信用户统一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String wxUnionid;

    /**
     * 微信用户标识
     */
    @Schema(description = "微信用户标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String openid;

    /**
     * 微信用户头像
     */
    @Schema(description = "微信用户头像", requiredMode = Schema.RequiredMode.REQUIRED)
    private String wxAvatarImg;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date updateTime;

    /**
     * 会员等级
     */
    @Schema(description = "会员卡背景图", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer grade;

    /**
     * 生日
     */
    @Schema(description = "生日", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date memberBirthday;


    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date finalTime;

    /**
     * 最后完成订单时间
     */
    @Schema(description = "最后完成订单时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date finalOrderFinishTime;

    /**
     * 用户类别 0 微信 1支付宝
     */
    @Schema(description = "用户类别 0 微信 1支付宝", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer memberCategory;

    /**
     * 分表字段 电话后两位取模
     */
    @Schema(description = "分表字段 电话后两位取模", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer shardingValue;

    @Schema(description = "会员卡背景图", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long projectOwnerShip;

}

