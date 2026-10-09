package com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryTask;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

import java.io.Serial;

@Data
@TableName("lottery_task")
public class LotteryTaskDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long activityId;

    private Long memberId;

    /**
     * 任务类型
     * 1 免费
     * 2 积分
     * 3 下单
     * 4 分享
     * 5 浏览首页
     */
    private Integer taskType;

    /**
     * 完成数
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
