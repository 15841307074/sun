package com.htyoudao.youdao.module.promotion.service.activityMjCommodity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMjCommodity.ActivityMjCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;

import java.util.List;

/**
 * @author Yangqinglin
 */
public interface ActivityMjCommodityService extends IService<ActivityMjCommodityDO> {
    void createBatch(List<Long> commodityIds, Long id);

    void deleteByActivityId(Long id);

    List<ActivityMjCommodityDO> selectByActivityId(Long id);

    List<CommodityDTO> listByActivityId(Long id);

}
