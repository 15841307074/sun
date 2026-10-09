package com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRewardDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ActivityAnswerRewardMapper extends BaseMapperX<ActivityAnswerRewardDO> {

    /**
     * 原子扣减奖励库存，total_num=0 表示不限库存。
     */
    @Update("""
            UPDATE activity_answer_reward
            SET used_num = COALESCE(used_num, 0) + 1
            WHERE id = #{id}
              AND deleted = 0
              AND (total_num = 0 OR COALESCE(used_num, 0) < total_num)
            """)
    int increaseUsedNum(@Param("id") Long id);

    /**
     * 物理删除本次未提交的奖励档位，避免逻辑删除数据继续占用唯一索引。
     */
    @Delete("""
            <script>
            DELETE FROM activity_answer_reward
            WHERE activity_id = #{activityId}
            <if test="correctCounts != null and correctCounts.size() > 0">
              AND correct_count NOT IN
              <foreach collection="correctCounts" item="correctCount" open="(" separator="," close=")">
                #{correctCount}
              </foreach>
            </if>
            </script>
            """)
    int deleteNotInCorrectCounts(@Param("activityId") Long activityId,
                                 @Param("correctCounts") List<Integer> correctCounts);

    /**
     * 物理删除同档位的冲突旧数据，保留本次确定要复用的记录。
     */
    @Delete("""
            <script>
            DELETE FROM activity_answer_reward
            WHERE activity_id = #{activityId}
              AND correct_count IN
              <foreach collection="correctCounts" item="correctCount" open="(" separator="," close=")">
                #{correctCount}
              </foreach>
            <if test="keepIds != null and keepIds.size() > 0">
              AND id NOT IN
              <foreach collection="keepIds" item="id" open="(" separator="," close=")">
                #{id}
              </foreach>
            </if>
            </script>
            """)
    int deleteConflictCorrectCounts(@Param("activityId") Long activityId,
                                    @Param("correctCounts") List<Integer> correctCounts,
                                    @Param("keepIds") List<Long> keepIds);
}
