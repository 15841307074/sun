package com.htyoudao.youdao.module.promotion.service.activityNjnz;


import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ActivityNjnzAppService {

    /**
     * 活动页
     *
     * @return
     */
    Map<Long, List<ActivityNjnzDTO>> selectNjnzActivity(Long storeId, Collection<Long> commodityIds);

    Map<Long, List<ActivityNjnzDTO>> selectNjnzActivity(Collection<Long> commodityIds,
                                                         List<ActivityDO> applicableActivities);
}
