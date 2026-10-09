package com.htyoudao.youdao.module.promotion.dal.mysql.survey;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionOptionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SurveyQuestionOptionMapper extends BaseMapperX<SurveyQuestionOptionDO> {

    default List<SurveyQuestionOptionDO> selectListByQuestionId(Long questionId) {
        return selectList(new LambdaQueryWrapperX<SurveyQuestionOptionDO>()
                .eq(SurveyQuestionOptionDO::getQuestionId, questionId)
                .orderByAsc(SurveyQuestionOptionDO::getSortOrder));
    }

    default List<SurveyQuestionOptionDO> selectListByQuestionIds(Collection<Long> questionIds) {
        return selectList(new LambdaQueryWrapperX<SurveyQuestionOptionDO>()
                .in(SurveyQuestionOptionDO::getQuestionId, questionIds)
                .orderByAsc(SurveyQuestionOptionDO::getSortOrder));
    }

    default void deleteByQuestionId(Long questionId) {
        delete(new LambdaQueryWrapperX<SurveyQuestionOptionDO>()
                .eq(SurveyQuestionOptionDO::getQuestionId, questionId));
    }

    default void deleteByQuestionIds(Collection<Long> questionIds) {
        delete(new LambdaQueryWrapperX<SurveyQuestionOptionDO>()
                .in(SurveyQuestionOptionDO::getQuestionId, questionIds));
    }
}
