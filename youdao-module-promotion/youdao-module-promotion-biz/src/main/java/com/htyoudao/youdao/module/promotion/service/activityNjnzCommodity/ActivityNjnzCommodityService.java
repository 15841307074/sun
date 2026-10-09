package com.htyoudao.youdao.module.promotion.service.activityNjnzCommodity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;

import java.util.List;

/**
 * @author villky
 */
public interface ActivityNjnzCommodityService extends IService<ActivityNjnzCommodityDO> {
    void createBatch(List<Long> commodityIds, Long id);

    void deleteByActivityId(Long id);

    List<ActivityNjnzCommodityDO> selectByActivityId(Long id);

    List<CommodityDTO> listByActivityId(Long id);

}
