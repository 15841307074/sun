package com.htyoudao.youdao.module.member.api.wxmember.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 小程序会员
 * @author dht
 */
@Data
public class WxMemberDTO implements Serializable {

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 手机号
     */
    private String memberMobile;

    /**
     * 用户身份 0非会员 1 会员
     */
    private Integer userIdentity;

    private String memberName;

    /** 会员昵称 */
    private String memberNickName;

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
     * 最后完成订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date finalOrderFinishTime;

    /**
     * 会员可用积分
     */
    private Integer memberIntegral;

    /**
     * 冻结积分数量（累计积分）
     */
    private Long integralFrozen;

    /**
     * 会员等级
     */
    private Integer memberLevel;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    private Date memberBirthday;

    /**
     * 会员状态 0正常 1冻结 2待注销 3已注销
     */
    private Integer memberStatus;

    /**
     * 用户类别 0 微信 1支付宝
     */
    private Integer memberCategory;

    /**
     * 会员状态：0-禁用，1-启用
     */
    private Integer state;

    /**
     * 项目编号
     */
    private Long businessId;

    /**
     * 分表字段 电话后两位取模
     */
    private Integer shardingValue;

    /**
     *  微信openId
     */
    private String openid;

    /**
     *  微信unionid
     */
    private String wxUnionid;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 优惠券来源 0 自领 1推广 2积分商城
     */
    private Integer couponSource;
}
