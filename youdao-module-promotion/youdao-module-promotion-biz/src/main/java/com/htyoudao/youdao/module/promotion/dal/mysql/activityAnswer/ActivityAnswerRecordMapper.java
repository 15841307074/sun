package com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityAnswerRecordMapper extends BaseMapperX<ActivityAnswerRecordDO> {

    /**
     * 按天统计答题次数和提交次数。
     */
    @Select("""
            SELECT
                DATE(start_time) AS statDate,
                COUNT(*) AS answerCount,
                SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS submitCount
            FROM activity_answer_record
            WHERE activity_id = #{activityId}
              AND deleted = 0
              AND start_time IS NOT NULL
              AND (#{startTime} IS NULL OR start_time >= #{startTime})
              AND (#{endTime} IS NULL OR start_time <= #{endTime})
            GROUP BY DATE(start_time)
            ORDER BY statDate
            """)
    List<Map<String, Object>> selectDailyAnswerStats(@Param("activityId") Long activityId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 按查询条件统计答题记录数量、完成提交数量和参与手机号数量。
     */
    @Select("""
            <script>
            SELECT
                COUNT(*) AS answerCount,
                SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS submitCount,
                COUNT(DISTINCT member_mobile) AS memberCount
            FROM activity_answer_record
            WHERE deleted = 0
            <if test="activityId != null">
              AND activity_id = #{activityId}
            </if>
            <if test="storeId != null">
              AND store_id = #{storeId}
            </if>
            <if test="statuses != null and statuses.size() > 0">
              AND status IN
              <foreach collection="statuses" item="status" open="(" separator="," close=")">
                #{status}
              </foreach>
            </if>
            <if test="startTime != null">
              AND start_time &gt;= #{startTime}
            </if>
            <if test="endTime != null">
              AND start_time &lt;= #{endTime}
            </if>
            <if test="memberMobile != null">
              AND CAST(member_mobile AS CHAR) LIKE CONCAT('%', #{memberMobile}, '%')
            </if>
            </script>
            """)
    Map<String, Object> selectRecordStatistics(@Param("activityId") Long activityId,
                                               @Param("memberMobile") Long memberMobile,
                                               @Param("storeId") Long storeId,
                                               @Param("statuses") List<Integer> statuses,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计活动参与人数，按手机号去重。
     */
    @Select("""
            SELECT COUNT(DISTINCT member_mobile)
            FROM activity_answer_record
            WHERE activity_id = #{activityId}
              AND deleted = 0
              AND member_mobile IS NOT NULL
            """)
    Long selectDistinctMemberMobileCount(@Param("activityId") Long activityId);
}
