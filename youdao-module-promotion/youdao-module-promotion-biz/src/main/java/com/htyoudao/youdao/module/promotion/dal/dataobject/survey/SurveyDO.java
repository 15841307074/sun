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
@TableName("survey")
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 问卷名称，限制30字 */
    private String surveyName;

    /** 问卷描述，限制1000字 */
    private String surveyDesc;

    /** 问卷封面图URL */
    private String coverImageUrl;

    /** 状态：0-未发布 1-进行中 2-已结束 */
    private Integer status;

    /** 时间控制：0-创建成功后即可填写（无结束时间） 1-按开始/结束时间控制 */
    private Integer timeControl;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 开始时间开关：0-关闭 1-开启（开启后 startTime 生效） */
    private Integer startTimeEnabled;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 结束时间开关：0-关闭 1-开启（开启后 endTime 生效） */
    private Integer endTimeEnabled;

    /** 未到开始时间提示语，最多50字 */
    private String startHint;

    /** 已结束提示语，最多50字 */
    private String endHint;

    /** 提交成功文案 */
    private String submitSuccessText;

    /** 是否需要登录后答题：0-否 1-是 */
    private Integer needLogin;

    /** 是否允许重复答卷：0-否 1-是 */
    private Integer allowRepeat;

    /** 重复答卷上限次数，默认10次 */
    private Integer repeatLimit;

    /** 社群专享：0-否 1-是 */
    private Integer communityOnly;

    /** 提交后显示感谢信息：0-否 1-是 */
    private Integer showThanks;

    /** 感谢信息文案 */
    private String thanksText;

    /** 提交后跳转页面地址 */
    private String redirectUrl;

    /** 店长企微码引导图URL */
    private String guideImageQr;

    /** 门店群活码引导图URL */
    private String guideImageGroup;

    /** 访问UV */
    private Integer uvCount;

    /** 提交人数 */
    private Integer submitCount;

    // ========== 分享配置字段 ==========


    /** 分享标题 */
    private String shareTitle;

    /** 分享图片URL */
    private String shareImgUrl;

    /** 分享内容 */
    private String shareNote;

    // ========== 奖励配置字段 ==========

    /** 奖励类型：0-无 1-积分 2-优惠券 3-优惠券包 */
    private Integer rewardType;

    /** 积分数（reward_type=1时有效），上限100 */
    private Integer rewardPoints;

    /** 优惠券/优惠券包ID */
    private Long couponId;

    /** 优惠券名称（冗余） */
    private String couponName;

    /** 发放方式：1-立即发放 2-按条件发放 */
    private Integer grantMode;

    /** 发放条件描述 */
    private String grantCondition;
}
