package com.htyoudao.youdao.module.promotion.service.activityMzCommodity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzCommodity.ActivityMzCommodityDO;

import java.util.List;

/**
 * 满赠活动关联商品 Service 接口
 */
public interface ActivityMzCommodityService extends IService<ActivityMzCommodityDO> {

    void createBatch(List<Long> commodityIds, Long activityId);

    void deleteByActivityId(Long activityId);

    List<ActivityMzCommodityDO> selectByActivityId(Long activityId);

    List<CommodityDTO> listByActivityId(Long activityId);
}
