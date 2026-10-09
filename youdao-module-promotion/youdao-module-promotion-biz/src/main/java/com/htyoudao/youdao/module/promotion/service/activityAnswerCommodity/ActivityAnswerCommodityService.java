package com.htyoudao.youdao.module.promotion.service.activityAnswerCommodity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityLotteryCommodity.ActivityLotteryCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;

import java.util.List;

/**
 * @author villky
 */
public interface ActivityAnswerCommodityService extends IService<ActivityAnswerCommodityDO> {
    void createBatch(List<Long> commodityIds, Long id);

    void deleteByActivityId(Long id);

    List<ActivityAnswerCommodityDO> selectByActivityId(Long id);

    List<CommodityDTO> listByActivityId(Long id);

}
