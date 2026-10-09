package com.htyoudao.youdao.module.member.controller.admin.wxmember.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.bigdecimal.BigDecimalStringConverter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.member.converter.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author dht
 */
@Data
public class WxMemberExcelRespVO {

    /**
     * 会员昵称
     */
    @ExcelProperty(value = "会员昵称", index = 0)
    private String memberNickName;

    /**
     * 手机号
     */
    @ExcelProperty(value = "联系方式", index = 1)
    private String memberMobile;

    /**
     * 手机号
     */
    @ExcelProperty(value = "会员标签", index = 2)
    private String memberTag;

    /**
     * 会员等级
     */
    @ExcelProperty(value = "会员等级", index = 3)
    private Integer grade;

    /**
     * 用户身份 0非会员 1 会员
     */
    @ExcelProperty(value = "会员类型", index = 4,converter = UserIdentityConverter.class)
    private Integer userIdentity;

    /**
     * 会员状态
     */
    @Schema(description = "会员状态 0正常 1冻结 2待注销 3已注销")
    @ExcelProperty(value = "会员状态", index = 5,converter = MemberStatusConverter.class)
    private Integer memberStatus;

    /**
     * 性别：0、保密；1、男；2、女
     */
    @ExcelProperty(value = "会员性别", index = 6,converter = GenderConverter.class)
    private Integer gender;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    @ExcelProperty(value = "会员生日", index = 7,converter = DateConverter.class)
    private Date memberBirthday;

    /**
     * 会员可用积分
     */
    @ExcelProperty(value = "会员可用积分", index = 8)
    private Integer memberIntegral;

    /**
     * 冻结积分数量
     */
    @ExcelProperty(value = "会员冻结积分", index = 9)
    private Long freezePoints;

    /**
     * 历史订单数
     */
    @Schema(description = "历史订单数量")
    @ExcelProperty(value = "历史订单数", index = 10)
    private Integer totalOrderNum;


    /**
     * 最后完成订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "最后下单时间", index = 11,converter = DateTimeConverter.class)
    private Date finalOrderFinishTime;

    /**
     * 用户类别 0 微信 1支付宝
     */
    @ExcelProperty(value = "注册平台", index = 12,converter = MemberCategoryConverter.class)
    private Integer memberCategory;

    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "注册时间", index = 13,converter = DateTimeConverter.class)
    private Date registerTime;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "最后登录时间", index = 14,converter = DateTimeConverter.class)
    private Date lastLoginTime;

    @Schema(description = "可用余额")
    @ExcelProperty(value = "余额", index = 15,converter = BigDecimalStringConverter.class)
    private BigDecimal balance;

    /**
     * 首次下单店铺名称
     */
    @ExcelProperty(value = "归属门店", index = 16)
    private String firstOrderStoreName;

    /**
     * 首次下单店铺名称
     */
    @ExcelProperty(value = "上级组织", index = 17)
    private String orgName;

    /**
     * 总积分数量
     */
    @ExcelIgnore
    private Long integralFrozen;

    /**
     * 首次下单店铺id
     */
    @ExcelIgnore
    private Long firstOrderStoreId;

    /**
     * id
     */
    @ExcelIgnore
    private Long memberId;
}
