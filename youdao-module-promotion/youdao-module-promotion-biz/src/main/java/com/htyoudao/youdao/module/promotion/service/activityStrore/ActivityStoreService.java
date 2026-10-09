package com.htyoudao.youdao.module.promotion.service.activityStrore;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;

import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import java.util.List;

/**
 * @author villky
 */
public interface ActivityStoreService extends IService<ActivityStoreDO> {
    void createBatch(List<Long> storeIds, Long id);

    void deleteByActivityId(Long id);

    List<ActivityStoreDO> selectByActivityId(Long id);

    List<StoreInfoDTO> storesByActivityId(Long id);

    List<Long> selectStoreIdsByActivityId(Long activityId);
}
