package com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value = "activity_vote_reward_log", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVoteRewardLogDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 活动主表ID */
    private Long activityId;
    /** 会员ID */
    private Long memberId;
    /** 会员手机号快照 */
    private Long memberMobile;
    /** 会员昵称快照 */
    private String memberName;
    /** 参与门店ID */
    private Long storeId;
    /** 参与门店名称 */
    private String storeName;
    /** 奖品类型 1积分 2优惠券 3优惠券包 4实物奖品 5现金红包 */
    private Integer prizeType;
    /** 奖品ID */
    private Long prizeId;
    /** 奖品名称快照 */
    private String prizeName;
    /** 奖品图片 */
    private String prizeImgUrl;
    /** 奖品价值 */
    private BigDecimal prizeValue;
    /** 红包领取状态 1未领取 2已领取 3已失效 */
    private Integer claimStatus;
    /** 红包调起支付参数 */
    private String packageInfo;
    /** 红包商户转账单号 */
    private String outBillNo;
    /** 奖品状态 0已发放 1未填写地址 2待发货 3已发货 9退回 */
    private Integer prizeState;
    /** 收货人 */
    private String receiveUser;
    /** 收货手机号 */
    private String receiveMobile;
    /** 收货地址 */
    private String receiveAddress;
    /** 快递公司 */
    private String expressCompany;
    /** 快递单号 */
    private String trackingNumber;
    /** 发放时间 */
    private LocalDateTime grantTime;
}
