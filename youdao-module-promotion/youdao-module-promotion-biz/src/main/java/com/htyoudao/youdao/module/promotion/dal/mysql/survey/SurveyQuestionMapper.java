package com.htyoudao.youdao.module.promotion.dal.mysql.survey;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SurveyQuestionMapper extends BaseMapperX<SurveyQuestionDO> {

    default List<SurveyQuestionDO> selectListBySurveyId(Long surveyId) {
        return selectList(new LambdaQueryWrapperX<SurveyQuestionDO>()
                .eq(SurveyQuestionDO::getSurveyId, surveyId)
                .orderByAsc(SurveyQuestionDO::getSortOrder));
    }

    default void deleteBySurveyId(Long surveyId) {
        delete(new LambdaQueryWrapperX<SurveyQuestionDO>()
                .eq(SurveyQuestionDO::getSurveyId, surveyId));
    }
}
