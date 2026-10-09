package com.htyoudao.youdao.module.promotion.dal.mysql.survey;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.FillBlankAnswerRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDetailDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SurveyAnswerDetailMapper extends BaseMapperX<SurveyAnswerDetailDO> {

    default List<SurveyAnswerDetailDO> selectListByAnswerId(Long answerId) {
        return selectList(new LambdaQueryWrapperX<SurveyAnswerDetailDO>()
                .eq(SurveyAnswerDetailDO::getAnswerId, answerId)
                .orderByAsc(SurveyAnswerDetailDO::getQuestionNo));
    }

    default List<SurveyAnswerDetailDO> selectListBySurveyIdAndQuestionId(Long surveyId, Long questionId) {
        return selectList(new LambdaQueryWrapperX<SurveyAnswerDetailDO>()
                .eq(SurveyAnswerDetailDO::getSurveyId, surveyId)
                .eq(SurveyAnswerDetailDO::getQuestionId, questionId));
    }

    default void deleteByAnswerId(Long answerId) {
        delete(new LambdaQueryWrapperX<SurveyAnswerDetailDO>()
                .eq(SurveyAnswerDetailDO::getAnswerId, answerId));
    }

    /**
     * 分页查询某道填空题的答案列表（关联答卷表获取提交时间和手机号）
     */
    @Select("<script>" +
            "SELECT d.answer_id AS answerId, a.phone, a.submit_time AS submitTime, d.fill_blank_text AS answerText " +
            "FROM survey_answer_detail d " +
            "INNER JOIN survey_answer a ON d.answer_id = a.id AND a.deleted = 0 " +
            "WHERE d.survey_id = #{surveyId} AND d.question_id = #{questionId} AND d.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND d.fill_blank_text LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "ORDER BY a.submit_time DESC" +
            "</script>")
    Page<FillBlankAnswerRespVO> selectFillBlankAnswerPage(Page<FillBlankAnswerRespVO> page,
                                                           @Param("surveyId") Long surveyId,
                                                           @Param("questionId") Long questionId,
                                                           @Param("keyword") String keyword);

    /**
     * 查询某道填空题的全部答案（不分页，用于导出）
     */
    @Select("SELECT d.answer_id AS answerId, a.phone, a.submit_time AS submitTime, d.fill_blank_text AS answerText " +
            "FROM survey_answer_detail d " +
            "INNER JOIN survey_answer a ON d.answer_id = a.id AND a.deleted = 0 " +
            "WHERE d.survey_id = #{surveyId} AND d.question_id = #{questionId} AND d.deleted = 0 " +
            "ORDER BY a.submit_time ASC")
    List<FillBlankAnswerRespVO> selectAllFillBlankAnswers(@Param("surveyId") Long surveyId,
                                                           @Param("questionId") Long questionId);
}
