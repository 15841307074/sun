package com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillTime;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.time.LocalDateTime;

@TableName("activity_seckill_time")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySeckillTimeDO extends BusinessBaseDO {
    private Long id;
    private Long activityId;
    //场次
    private Integer times;

    private Integer startTime;
    private Integer endTime;
}
