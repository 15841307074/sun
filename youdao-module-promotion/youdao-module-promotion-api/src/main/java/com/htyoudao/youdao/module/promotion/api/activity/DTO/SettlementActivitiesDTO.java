package com.htyoudao.youdao.module.promotion.api.activity.DTO;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** 结算所需营销活动批量查询结果。 */
@Data
public class SettlementActivitiesDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<Long, List<ActivityNjnzDTO>> njnzActivities;
    private Map<Long, List<ActivityMJDTO>> mjActivities;
    private Map<Long, List<ActivityMzDTO>> mzActivities;
}
