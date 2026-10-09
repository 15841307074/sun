package com.htyoudao.youdao.module.promotion.service.activityChannel;

import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivityChannelSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface ActivityChannelService {
    List<ActivityChannelDO> selectByActivityId(Long id);

    void deleteByActivityId(Long id);


    void createBatch(List<ActivityChannelDO> activityChannelDOList);


    void updateBatch(List<ActivityChannelDO> activityChannelDOList1);

    void createChannelDO(Long activityId,int type);

   // void createAndUpdateChannel(@NotNull(message = "活动推广相关链接 不能为空") @Valid List<ActivityChannelSaveReqVO> activityChannelList, Long id,int type);

    void  creatChannle(Long businessId,String sortPath);

    List<ActivityChannelDO> selectByChannelId(Long id);

    List<ActivityChannelDO> updateStatusByChannelId(Long id, Integer isEnable);

    List<ActivityChannelDO> selectByActivityIdWithIsEnable(Long id);

    void deleteByChannelId(Long id);

    void updateNameByChannelId(Long id, String name);

    void refreshByActivityId(Long activityId, List<Long> channelIds, Integer type);

    void initialization();

}
