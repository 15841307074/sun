package com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

import java.io.Serial;

@Data
@TableName("activity_task")
public class ActivityTaskDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 任务完成次数
     */
    private Integer finishCount;

    /**
     * 获得次数
     */
    private Integer gainCount;

    /**
     * 消耗次数
     */
    private Integer consumeCount;
}
