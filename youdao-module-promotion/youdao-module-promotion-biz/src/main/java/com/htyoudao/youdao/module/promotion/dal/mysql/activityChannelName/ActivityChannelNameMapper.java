package com.htyoudao.youdao.module.promotion.dal.mysql.activityChannelName;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo.ActivityChannelNamePageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannelName.ActivityChannelNameDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 营销活动渠道名称 Mapper
 */
@Mapper
public interface ActivityChannelNameMapper extends BaseMapperX<ActivityChannelNameDO> {

    default PageResult<ActivityChannelNameDO> selectPage(ActivityChannelNamePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ActivityChannelNameDO>()
                .likeIfPresent(ActivityChannelNameDO::getName, reqVO.getName())
                .eqIfPresent(ActivityChannelNameDO::getIsEnable, reqVO.getIsEnable())
                .orderByAsc(ActivityChannelNameDO::getCreateTime));
    }

    @Select("select id as id,name as name from activity_channel_name")
    List<ActivityChannelNameDO> selectAll();
}

