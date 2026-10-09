package com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 有奖问答任务记录。
 */
@Data
@TableName("activity_answer_task")
public class ActivityAnswerTaskDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动 ID。
     */
    private Long activityId;

    /**
     * 会员 ID。
     */
    private Long memberId;

    /**
     * 会员手机号快照。
     */
    private Long memberMobile;

    /**
     * 任务类型 3下单 4分享 5浏览首页 6签到。
     */
    private Integer taskType;

    /**
     * 活动场次/次数周期标识。
     */
    private String periodKey;

    /**
     * 任务日期，用于按天查询。
     */
    private LocalDate taskDate;

    /**
     * 完成次数。
     */
    private Integer finishCount;

    /**
     * 获得答题次数。
     */
    private Integer gainCount;

    /**
     * 消耗答题次数。
     */
    private Integer consumeCount;
}
