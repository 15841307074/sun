package com.htyoudao.youdao.module.promotion.service.activityJDCommodity;

import com.htyoudao.youdao.module.promotion.api.seckill.DTO.ActivityCommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillCommodity.ActivitySeckillCommodityDO;

import java.util.List;

public interface ActivityJDCommodityService {
    void createBatch(List<ActivityJDCommodityDO> activityJDCommodityDOS);
    void updateBatch(List<ActivityJDCommodityDO> activityJDCommodityDOS);
    List<ActivityJDCommodityDO> selectByActivityId(Long activityId);
    void deleteByActivityId(Long id);

}
