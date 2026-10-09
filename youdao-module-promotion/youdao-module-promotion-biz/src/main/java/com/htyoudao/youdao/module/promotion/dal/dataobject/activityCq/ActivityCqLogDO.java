package com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDateTime;

@TableName(value = "activity_cq_log", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCqLogDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 签码
     */
    private String signCode;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 会员昵称
     */
    private String memberName;

    /**
     * 联系方式
     */
    private String memberMobile;

    /**
     * 性别：0、保密；1、男；2、女
     */
    private Integer gender;

    /**
     * 用户类别 0 微信 1支付宝
     */
    private Integer memberCategory;

    /**
     * 获取方式
     */
    private Integer obtainType;

    /**
     * 抽签门店id
     */
    private Long storeId;

    /**
     * 抽签门店名称
     */
    private String storeName;

    /**
     * 抽签时间
     */
    private LocalDateTime drawTime;

    /**
     * 结果状态
     */
    private Integer resultStatus;

    /**
     * 奖品id
     */
    private Long prizeId;

    /**
     * 奖品类型
     */
    private Integer prizeType;

    /**
     * 奖品内容
     */
    private String prizeContent;

    /**
     * 奖品图片
     */
    private String prizeImgUrl;

    /**
     * 商户转账单号
     */
    private String outBillNo;

    /**
     * 红包领取状态（1 未领取 2 已领取 3 已过期）
     */
    private Integer claimStatus;

    /**
     * 红包返参链接
     */
    private String packageInfo;

    /**
     * 奖品状态
     */
    private Integer prizeState;

    /**
     * 收件人
     */
    private String receiveUser;

    /**
     * 收件联系方式
     */
    private String receiveMobile;

    /**
     * 收件地址
     */
    private String receiveAddress;

    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 快递公司
     */
    private String expressCompany;
}
