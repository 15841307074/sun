package com.htyoudao.youdao.module.promotion.dal.mysql.survey;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.SurveyPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface SurveyMapper extends BaseMapperX<SurveyDO> {

    default PageResult<SurveyDO> selectPage(SurveyPageReqVO reqVO) {
        LambdaQueryWrapperX<SurveyDO> wrapper = new LambdaQueryWrapperX<SurveyDO>()
                .likeIfPresent(SurveyDO::getSurveyName, reqVO.getSurveyName())
                .betweenIfPresent(SurveyDO::getStartTime, cleanEpoch(reqVO.getStartTimeBegin()), cleanEpoch(reqVO.getStartTimeEnd()))
                .betweenIfPresent(SurveyDO::getEndTime, cleanEpoch(reqVO.getEndTimeBegin()), cleanEpoch(reqVO.getEndTimeEnd()))
                .orderByDesc(SurveyDO::getCreateTime);

        // 状态筛选：前端传的是计算字段 status（0-未发布 1-进行中 2-已结束），需转换为 DB 条件
        if (reqVO.getStatus() != null) {
            switch (reqVO.getStatus()) {
                case 0: // 未发布：无时间且DB=0，或有开始时间但未到，或两个都有但未到开始
                    wrapper.and(w -> w
                        .and(noTime -> noTime
                            .in(SurveyDO::getStatus, 0)
                            .isNull(SurveyDO::getStartTime)
                            .isNull(SurveyDO::getEndTime))
                        .or(onlyStart -> onlyStart
                            .isNotNull(SurveyDO::getStartTime)
                            .isNull(SurveyDO::getEndTime)
                            .apply("start_time > NOW()"))
                        .or(both -> both
                            .isNotNull(SurveyDO::getStartTime)
                            .isNotNull(SurveyDO::getEndTime)
                            .apply("start_time > NOW()"))
                    );
                    break;
                case 1: // 进行中：无时间且DB=1，或时间范围内
                    wrapper.and(w -> w
                        .and(noTime -> noTime
                            .eq(SurveyDO::getStatus, 1)
                            .isNull(SurveyDO::getStartTime)
                            .isNull(SurveyDO::getEndTime))
                        .or(onlyStart -> onlyStart
                            .isNotNull(SurveyDO::getStartTime)
                            .isNull(SurveyDO::getEndTime)
                            .apply("start_time <= NOW()"))
                        .or(onlyEnd -> onlyEnd
                            .isNull(SurveyDO::getStartTime)
                            .isNotNull(SurveyDO::getEndTime)
                            .apply("end_time >= NOW()"))
                        .or(both -> both
                            .isNotNull(SurveyDO::getStartTime)
                            .isNotNull(SurveyDO::getEndTime)
                            .apply("start_time <= NOW() AND end_time >= NOW()"))
                    );
                    break;
                case 2: // 已结束：无时间且DB=2，或已过结束时间
                    wrapper.and(w -> w
                        .and(noTime -> noTime
                            .eq(SurveyDO::getStatus, 2)
                            .isNull(SurveyDO::getStartTime)
                            .isNull(SurveyDO::getEndTime))
                        .or(onlyEnd -> onlyEnd
                            .isNull(SurveyDO::getStartTime)
                            .isNotNull(SurveyDO::getEndTime)
                            .apply("end_time < NOW()"))
                        .or(both -> both
                            .isNotNull(SurveyDO::getStartTime)
                            .isNotNull(SurveyDO::getEndTime)
                            .apply("end_time < NOW()"))
                    );
                    break;
                default:
                    break;
            }
        }

        return selectPage(reqVO, wrapper);
    }

    /** 清理 epoch 0 脏值：全局反序列化器会把前端空字符串转为 1970-01-01 08:00:00，当作 null 处理 */
    private static LocalDateTime cleanEpoch(LocalDateTime time) {
        return time != null && time.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() == 0 ? null : time;
    }

    /**
     * 提交人数原子递增 +1（提交答卷时直接调用）
     */
    @Update("UPDATE survey SET submit_count = submit_count + 1 WHERE id = #{surveyId} AND deleted = 0")
    void incrementSubmitCount(@Param("surveyId") Long surveyId);

    /**
     * UV 回算：从 survey_answer 表 COUNT(DISTINCT phone)，取 GREATEST 保证单调递增（可用于手动回算）
     */
    @Update("UPDATE survey s SET s.uv_count = GREATEST(IFNULL(s.uv_count, 0), " +
            "(SELECT COUNT(DISTINCT phone) FROM survey_answer WHERE survey_id = #{surveyId} AND deleted = 0)) " +
            "WHERE s.id = #{surveyId} AND s.deleted = 0")
    void recalculateUvCount(@Param("surveyId") Long surveyId);

    @Update("UPDATE survey s SET s.uv_count = GREATEST(IFNULL(s.uv_count, 0), #{uvCount}) " +
            "WHERE s.id = #{surveyId} AND s.deleted = 0")
    void updateUvCount(@Param("surveyId") Long surveyId, @Param("uvCount") Long uvCount);
}
