package com.htyoudao.youdao.module.order.core.submit.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ActivityBaseDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7469690377646616266L;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动类型
     */
    private Integer activityType;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 优惠标签
     */
    private String activityTag;
}
