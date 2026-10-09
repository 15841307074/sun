package com.htyoudao.youdao.module.promotion.dal.dataobject.survey;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("survey_answer")
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 问卷ID */
    private Long surveyId;

    /** 手机号 */
    private Long phone;

    /** 答题用户ID（ */
    private Long memberId;

    /** 用户openid（用于去重和奖励发放） */
    private String openid;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 来源：1-微信 2-支付宝 */
    private Integer source;

    /** 来源IP */
    private String sourceIp;

    /** 答题所用秒数 */
    private Integer duration;

    /** 是否有效答卷：0-无效 1-有效 */
    private Integer isValid;

    // ========== 奖励发放信息 ==========

    /** 奖励类型：0-无 1-积分 2-优惠券 3-优惠券包 */
    private Integer rewardType;

    /** 奖励值（积分数量/优惠券ID等） */
    private String rewardValue;

    /** 奖励状态：0-未发放 1-已发放 2-发放失败 */
    private Integer rewardStatus;

    /** 发放失败原因 */
    private String rewardFailReason;

    /** 实际发放时间 */
    private LocalDateTime rewardGrantedAt;

}
