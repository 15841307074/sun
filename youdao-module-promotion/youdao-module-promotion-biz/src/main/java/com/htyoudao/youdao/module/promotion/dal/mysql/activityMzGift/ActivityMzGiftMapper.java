package com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift;

import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 满赠活动赠送商品 Mapper
 */
@Mapper
public interface ActivityMzGiftMapper extends BaseMapperX<ActivityMzGiftDO> {

    default List<ActivityMzGiftDO> selectByActivityIds(List<Long> activityIds) {
        return selectList(ActivityMzGiftDO::getActivityId, activityIds);
    }

    default List<ActivityMzGiftDO> selectApplicableGifts(Collection<Long> sharedActivityIds,
                                                          Collection<Long> independentActivityIds,
                                                          Long storeId) {
        LambdaQueryWrapperX<ActivityMzGiftDO> query = new LambdaQueryWrapperX<>();
        query.and(wrapper -> {
            boolean hasShared = sharedActivityIds != null && !sharedActivityIds.isEmpty();
            boolean hasIndependent = independentActivityIds != null && !independentActivityIds.isEmpty();
            if (hasShared) {
                wrapper.in(ActivityMzGiftDO::getActivityId, sharedActivityIds)
                        .isNull(ActivityMzGiftDO::getStoreId);
            }
            if (hasIndependent) {
                if (hasShared) {
                    wrapper.or();
                }
                wrapper.in(ActivityMzGiftDO::getActivityId, independentActivityIds)
                        .eq(ActivityMzGiftDO::getStoreId, storeId);
            }
        });
        return selectList(query);
    }
}
