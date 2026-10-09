package com.htyoudao.youdao.module.promotion.dal.mysql.activityNjnzCommodity;

import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @author villky
 */
@Mapper
public interface ActivityNjnzCommodityMapper extends BaseMapperX<ActivityNjnzCommodityDO> {

    default List<ActivityNjnzCommodityDO> selectByActivityIds(@Param("activityIds") List<Long> activityIds){
        return selectList(ActivityNjnzCommodityDO::getActivityId, activityIds);
    }

    default List<ActivityNjnzCommodityDO> selectByActivityIdsAndCommodityIds(
            Collection<Long> activityIds, Collection<Long> commodityIds) {
        return selectList(new LambdaQueryWrapperX<ActivityNjnzCommodityDO>()
                .in(ActivityNjnzCommodityDO::getActivityId, activityIds)
                .in(ActivityNjnzCommodityDO::getCommodityId, commodityIds));
    }

}
