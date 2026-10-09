package com.htyoudao.youdao.module.promotion.dal.mysql.activityMjCommodity;

import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMjCommodity.ActivityMjCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author Yangqinglin
 */
@Mapper
public interface ActivityMjCommodityMapper extends BaseMapperX<ActivityMjCommodityDO> {

    default List<ActivityMjCommodityDO> selectByActivityIds(@Param("activityIds") List<Long> activityIds){
        return selectList(ActivityMjCommodityDO::getActivityId, activityIds);
    }

    default List<ActivityMjCommodityDO> selectByActivityIdsAndCommodityIds(
            Collection<Long> activityIds, Collection<Long> commodityIds) {
        return selectList(new LambdaQueryWrapperX<ActivityMjCommodityDO>()
                .in(ActivityMjCommodityDO::getActivityId, activityIds)
                .in(ActivityMjCommodityDO::getCommodityId, commodityIds));
    }

}
