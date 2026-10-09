package com.htyoudao.youdao.module.promotion.service.activityJkCommodity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMjCommodity.ActivityMjCommodityDO;

import java.util.List;

/**
 * @author Yangqinglin
 */
public interface ActivityJkCommodityService extends IService<ActivityJkCommodityDO> {
    void createBatch(List<Long> commodityIds, Long id);

    void deleteByActivityId(Long id);

    List<ActivityJkCommodityDO> selectByActivityId(Long id);

    List<CommodityDTO> listByActivityId(Long id);

}
