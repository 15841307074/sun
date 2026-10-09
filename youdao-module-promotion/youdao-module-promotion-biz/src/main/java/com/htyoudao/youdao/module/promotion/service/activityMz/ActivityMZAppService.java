package com.htyoudao.youdao.module.promotion.service.activityMz;


import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMzDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ActivityMZAppService {

    /**
     * 活动页
     *
     * @return
     */
    Map<Long, List<ActivityMzDTO>> selectMZActivity(Long storeId, Collection<Long> commodityIds);

    Map<Long, List<ActivityMzDTO>> selectMZActivity(Long storeId, Collection<Long> commodityIds,
                                                    List<ActivityDO> applicableActivities);
}
