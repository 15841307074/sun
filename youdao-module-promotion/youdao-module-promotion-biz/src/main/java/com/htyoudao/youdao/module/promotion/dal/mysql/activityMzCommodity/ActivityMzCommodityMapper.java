package com.htyoudao.youdao.module.promotion.dal.mysql.activityMzCommodity;

import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzCommodity.ActivityMzCommodityDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 满赠活动关联商品 Mapper
 */
@Mapper
public interface ActivityMzCommodityMapper extends BaseMapperX<ActivityMzCommodityDO> {

    default List<ActivityMzCommodityDO> selectByActivityIds(List<Long> activityIds) {
        return selectList(ActivityMzCommodityDO::getActivityId, activityIds);
    }

    default List<ActivityMzCommodityDO> selectByActivityIdsAndCommodityIds(
            Collection<Long> activityIds, Collection<Long> commodityIds) {
        return selectList(new LambdaQueryWrapperX<ActivityMzCommodityDO>()
                .in(ActivityMzCommodityDO::getActivityId, activityIds)
                .in(ActivityMzCommodityDO::getCommodityId, commodityIds));
    }
}
