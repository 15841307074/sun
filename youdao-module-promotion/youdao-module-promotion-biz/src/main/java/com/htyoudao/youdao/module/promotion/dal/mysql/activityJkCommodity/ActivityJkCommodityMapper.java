package com.htyoudao.youdao.module.promotion.dal.mysql.activityJkCommodity;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMjCommodity.ActivityMjCommodityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Yangqinglin
 */
@Mapper
public interface ActivityJkCommodityMapper extends BaseMapperX<ActivityJkCommodityDO> {

    default List<ActivityJkCommodityDO> selectByActivityIds(@Param("activityIds") List<Long> activityIds){
        return selectList(ActivityJkCommodityDO::getActivityId, activityIds);
    }

}
