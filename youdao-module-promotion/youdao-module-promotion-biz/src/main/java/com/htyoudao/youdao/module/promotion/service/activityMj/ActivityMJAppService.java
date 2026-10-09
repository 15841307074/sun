package com.htyoudao.youdao.module.promotion.service.activityMj;


import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMJDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ActivityMJAppService {

    /**
     * 活动页
     *
     * @return
     */
    Map<Long, List<ActivityMJDTO>> selectMJActivity(Long storeId, Collection<Long> commodityIds);

    Map<Long, List<ActivityMJDTO>> selectMJActivity(Collection<Long> commodityIds,
                                                     List<ActivityDO> applicableActivities);

}
