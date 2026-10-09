package com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRewardLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityAnswerRewardLogMapper extends BaseMapperX<ActivityAnswerRewardLogDO> {

    /**
     * 按奖励档位统计已领取/已发放数量。
     */
    @Select("""
            SELECT
                reward_id AS rewardId,
                COUNT(*) AS usedCount
            FROM activity_answer_reward_log
            WHERE activity_id = #{activityId}
              AND reward_id IS NOT NULL
              AND deleted = 0
              AND (
                    (prize_type = 5 AND claim_status IN (1, 2))
                    OR
                    ((prize_type <> 5 OR prize_type IS NULL) AND (prize_state IS NULL OR prize_state <> 9))
                  )
            GROUP BY reward_id
            """)
    List<Map<String, Object>> selectRewardUsedCountGroupByRewardId(@Param("activityId") Long activityId);

    /**
     * 按奖励档位和门店统计已领取/已发放数量。
     */
    @Select("""
            SELECT
                reward_id AS rewardId,
                store_id AS storeId,
                COUNT(*) AS usedCount
            FROM activity_answer_reward_log
            WHERE activity_id = #{activityId}
              AND reward_id IS NOT NULL
              AND store_id IS NOT NULL
              AND deleted = 0
              AND (
                    (prize_type = 5 AND claim_status IN (1, 2))
                    OR
                    ((prize_type <> 5 OR prize_type IS NULL) AND (prize_state IS NULL OR prize_state <> 9))
                  )
            GROUP BY reward_id, store_id
            """)
    List<Map<String, Object>> selectRewardUsedCountGroupByRewardIdAndStoreId(@Param("activityId") Long activityId);

    /**
     * 按查询条件统计发放次数和发放手机号数量。
     */
    @Select("""
            <script>
            SELECT
                COUNT(*) AS rewardCount,
                COUNT(DISTINCT member_mobile) AS memberCount
            FROM activity_answer_reward_log
            WHERE deleted = 0
            <if test="activityId != null">
              AND activity_id = #{activityId}
            </if>
            <if test="prizeType != null">
              AND prize_type = #{prizeType}
            </if>
            <if test="prizeState != null">
              AND prize_state = #{prizeState}
            </if>
            <if test="startTime != null">
              AND grant_time &gt;= #{startTime}
            </if>
            <if test="endTime != null">
              AND grant_time &lt;= #{endTime}
            </if>
            <if test="memberMobile != null">
              AND CAST(member_mobile AS CHAR) LIKE CONCAT('%', #{memberMobile}, '%')
            </if>
            <if test="claimStatus != null">
              AND prize_type = 5
              <choose>
                <when test="claimStatus == 1">
                  AND claim_status = 1
                  AND grant_time &gt; #{redPacketExpireTime}
                </when>
                <when test="claimStatus == 2">
                  AND claim_status = 2
                </when>
                <when test="claimStatus == 3">
                  AND claim_status = 1
                  AND grant_time &lt;= #{redPacketExpireTime}
                </when>
              </choose>
            </if>
            <if test="addressStatus != null and addressStatus == 0">
              AND prize_type = 4
              AND (receive_address IS NULL OR receive_address = '')
            </if>
            <if test="addressStatus != null and addressStatus == 1">
              AND prize_type = 4
              AND receive_address IS NOT NULL
              AND receive_address != ''
            </if>
            <if test="expressStatus != null and expressStatus == 0">
              AND prize_type = 4
              AND (tracking_number IS NULL OR tracking_number = '')
            </if>
            <if test="expressStatus != null and expressStatus == 1">
              AND prize_type = 4
              AND tracking_number IS NOT NULL
              AND tracking_number != ''
            </if>
            </script>
            """)
    Map<String, Object> selectRewardLogStatistics(@Param("activityId") Long activityId,
                                                  @Param("memberMobile") Long memberMobile,
                                                  @Param("prizeType") Integer prizeType,
                                                  @Param("prizeState") Integer prizeState,
                                                  @Param("claimStatus") Integer claimStatus,
                                                  @Param("addressStatus") Integer addressStatus,
                                                  @Param("expressStatus") Integer expressStatus,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("redPacketExpireTime") LocalDateTime redPacketExpireTime);
}
