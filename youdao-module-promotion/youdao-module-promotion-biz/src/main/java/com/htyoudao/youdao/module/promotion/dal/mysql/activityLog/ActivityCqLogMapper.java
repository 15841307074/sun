package com.htyoudao.youdao.module.promotion.dal.mysql.activityLog;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqPendingMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ActivityCqLogMapper extends BaseMapperX<ActivityCqLogDO> {

    @Select("""
            <script>
            SELECT member_id AS memberId, COUNT(1) AS signCount
            FROM activity_cq_log
            WHERE deleted = 0
              AND activity_id = #{activityId}
              AND result_status = #{resultStatus}
              AND member_id IS NOT NULL
              AND member_mobile IS NOT NULL
              AND TRIM(member_mobile) != ''
            GROUP BY member_id
            ORDER BY member_id
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<ActivityCqPendingMemberDO> selectPendingMemberPage(@Param("activityId") Long activityId,
                                                            @Param("resultStatus") Integer resultStatus,
                                                            @Param("offset") Integer offset,
                                                            @Param("pageSize") Integer pageSize);

    @Select("""
            <script>
            SELECT member_id AS memberId, COUNT(1) AS signCount
            FROM activity_cq_log
            WHERE deleted = 0
              AND activity_id = #{activityId}
              AND result_status = #{resultStatus}
              AND member_id IS NOT NULL
              AND member_mobile IS NOT NULL
              AND TRIM(member_mobile) != ''
              AND (member_category IS NULL OR member_category != #{excludedMemberCategory})
            GROUP BY member_id
            ORDER BY member_id
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<ActivityCqPendingMemberDO> selectPendingMemberPageExcludeMemberCategory(@Param("activityId") Long activityId,
                                                                                 @Param("resultStatus") Integer resultStatus,
                                                                                 @Param("excludedMemberCategory") Integer excludedMemberCategory,
                                                                                 @Param("offset") Integer offset,
                                                                                 @Param("pageSize") Integer pageSize);

    @Select("""
            SELECT *
            FROM activity_cq_log
            WHERE deleted = 0
              AND activity_id = #{activityId}
              AND member_id = #{memberId}
              AND result_status = #{resultStatus}
              AND member_mobile IS NOT NULL
              AND TRIM(member_mobile) != ''
            ORDER BY create_time ASC, id ASC
            LIMIT #{offset}, 1
            """)
    ActivityCqLogDO selectPendingLogByOffset(@Param("activityId") Long activityId,
                                             @Param("memberId") Long memberId,
                                             @Param("resultStatus") Integer resultStatus,
                                             @Param("offset") Integer offset);
}
