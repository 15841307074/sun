package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import lombok.Data;

import java.util.Date;

@Data
public class WxMemberVO {

    /** 会员卡背景图 */
    private String backgroundImage;

    /** 用户头像 */
    private String memberAvatar;

    /** 会员昵称 */
    private String memberNickName;

    /** 会员可用积分 */
    private Long allPoints;

    /** 冻结积分数量 */
    private Long integralFrozen;

    /** 积分最高门槛 */
    private Long maxPointsThreshold;

    /** 会员卡描述 */
    private String description;

    private Integer memberLevel;
    /**
     * 会员积分
     */
    private Integer memberIntegral;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 用户名（登录名称）
     */
    private String memberName;


    /**
     * 手机号
     */
    private String memberMobile;




    /**
     * 性别：0、保密；1、男；2、女
     */
    private Integer gender;

    /**
     * 注册时间
     */
    private Date registerTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 登录次数
     */
    private Integer loginNumber;

    /**
     * 会员来源 0普通下单
     */
    private String memberChannel;

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
    private Date updateTime;

    /**
     * 会员等级
     */
    private Integer grade;

    /**
     * 生日
     */
    private Date memberBirthday;


    /**
     * 最后修改时间
     */
    private Date finalTime;

    /**
     * 最后完成订单时间
     */
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
     * 项目id
     */
    private Long businessId;

}

