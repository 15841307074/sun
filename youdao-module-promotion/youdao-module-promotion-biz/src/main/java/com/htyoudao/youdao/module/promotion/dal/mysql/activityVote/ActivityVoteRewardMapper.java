package com.htyoudao.youdao.module.promotion.dal.mysql.activityVote;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteRewardDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ActivityVoteRewardMapper extends BaseMapperX<ActivityVoteRewardDO> {

    @Delete("DELETE FROM activity_vote_reward WHERE activity_id = #{activityId}")
    int physicalDeleteByActivityId(@Param("activityId") Long activityId);
}
