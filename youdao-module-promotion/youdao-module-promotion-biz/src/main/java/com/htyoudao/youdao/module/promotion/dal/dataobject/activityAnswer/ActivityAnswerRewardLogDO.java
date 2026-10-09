package com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("activity_answer_reward_log")
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerRewardLogDO extends BusinessBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "答题记录ID")
    private Long recordId;

    @Schema(description = "答题编号")
    private String answerNo;

    @Schema(description = "活动主表ID")
    private Long activityId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员昵称")
    private String memberName;

    @Schema(description = "会员手机号")
    private Long memberMobile;

    @Schema(description = "参与门店ID")
    private Long storeId;

    @Schema(description = "参与门店名称")
    private String storeName;

    @Schema(description = "奖励档位ID")
    private Long rewardId;

    @Schema(description = "实际命中奖励档位答对题数")
    private Integer rewardCorrectCount;

    @Schema(description = "奖品类型 1积分 2优惠券 3优惠券包 4实物 5现金红包")
    private Integer prizeType;

    @Schema(description = "奖品ID")
    private Long prizeId;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    @Schema(description = "领取状态 1未领取 2已领取 3已失效")
    private Integer claimStatus;

    @Schema(description = "红包调起支付参数")
    private String packageInfo;

    @Schema(description = "红包商户转账单号")
    private String outBillNo;

    @Schema(description = "奖品状态 0已发放 1未填写地址 2待发货 3已发货 9退回")
    private Integer prizeState;

    @Schema(description = "收件人")
    private String receiveUser;

    @Schema(description = "收件电话")
    private String receiveMobile;

    @Schema(description = "收货地址")
    private String receiveAddress;

    @Schema(description = "快递公司")
    private String expressCompany;

    @Schema(description = "快递单号")
    private String trackingNumber;

    @Schema(description = "发放时间")
    private LocalDateTime grantTime;
}
