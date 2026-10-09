package com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRecordDetailDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityAnswerRecordDetailMapper extends BaseMapperX<ActivityAnswerRecordDetailDO> {

    /**
     * 按答题记录参与时间筛选，并按活动ID和题目名称统计作答情况。
     */
    @Select("""
            SELECT
                MIN(d.question_id) AS questionId,
                d.question_title AS questionTitle,
                COUNT(*) AS answerCount,
                SUM(CASE WHEN d.answer_result = 1 THEN 1 ELSE 0 END) AS correctCount,
                SUM(CASE WHEN d.answer_result = 0 THEN 1 ELSE 0 END) AS wrongCount
            FROM activity_answer_record r
            INNER JOIN activity_answer_record_detail d ON d.record_id = r.id AND d.deleted = 0
            WHERE r.activity_id = #{activityId}
              AND d.is_answered = 1
              AND (#{startTime} IS NULL OR r.start_time >= #{startTime})
              AND (#{endTime} IS NULL OR r.start_time <= #{endTime})
            GROUP BY r.activity_id, d.question_title
            ORDER BY MIN(d.create_time), MIN(d.id)
            """)
    List<Map<String, Object>> selectQuestionAnalysis(@Param("activityId") Long activityId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);
}
