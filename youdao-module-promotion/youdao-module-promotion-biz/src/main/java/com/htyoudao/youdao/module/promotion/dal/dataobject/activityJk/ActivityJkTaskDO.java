package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;

@Data
@TableName("activity_jk_task")
public class ActivityJkTaskDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long activityId;

    private Long memberId;
    /**
     * 任务类型（1 签到 2 下单 3 分享助力）
     */
    private Integer taskType;
    /**
     * '任务完成数'
     */
    private Integer finishCount;
    /**
     * '获得次数'
     */
    private Integer gainCount;
    /**
     * '消耗次数'
     */
    private Integer consumeCount;
}
