package com.htyoudao.youdao.module.promotion.service.activityLotteryCommodity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityLotteryCommodity.ActivityLotteryCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;

import java.util.List;

/**
 * @author villky
 */
public interface ActivityLotteryCommodityService extends IService<ActivityLotteryCommodityDO> {
    void createBatch(List<Long> commodityIds, Long id);

    void deleteByActivityId(Long id);

    List<ActivityLotteryCommodityDO> selectByActivityId(Long id);

    List<CommodityDTO> listByActivityId(Long id);

}
