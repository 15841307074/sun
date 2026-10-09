package com.htyoudao.youdao.module.promotion.service.activityMzGift;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;

import java.util.List;

/**
 * 满赠活动赠送商品 Service 接口
 */
public interface ActivityMzGiftService extends IService<ActivityMzGiftDO> {

    void createBatch(List<ActivityMzGiftDO> giftList, Long activityId);

    void createBatchWithStoreId(List<ActivityMzGiftDO> giftList, Long activityId, Long storeId);

    void deleteByActivityId(Long activityId);

    void deleteByActivityIdAndStoreIds(Long activityId, List<Long> storeIds);

    List<ActivityMzGiftDO> selectByActivityId(Long activityId);

    List<ActivityMzGiftDO> selectByActivityIdAndStoreId(Long activityId, Long storeId);

    List<CommodityDTO> listGiftCommodityByActivityId(Long activityId);
}
