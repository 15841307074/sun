package com.htyoudao.youdao.module.promotion.dal.mysql.survey;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.SurveyAnswerPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SurveyAnswerMapper extends BaseMapperX<SurveyAnswerDO> {

    default PageResult<SurveyAnswerDO> selectPage(SurveyAnswerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SurveyAnswerDO>()
                .eqIfPresent(SurveyAnswerDO::getSurveyId, reqVO.getSurveyId())
                .eqIfPresent(SurveyAnswerDO::getRewardStatus, reqVO.getRewardStatus())
                .betweenIfPresent(SurveyAnswerDO::getSubmitTime, reqVO.getSubmitTimeBegin(), reqVO.getSubmitTimeEnd())
                .orderByDesc(SurveyAnswerDO::getSubmitTime));
    }

    default Long selectCountBySurveyId(Long surveyId) {
        return selectCount(new LambdaQueryWrapperX<SurveyAnswerDO>()
                .eq(SurveyAnswerDO::getSurveyId, surveyId));
    }

    default boolean existsBySurveyIdAndPhone(Long surveyId, Long phone) {
        return selectCount(new LambdaQueryWrapperX<SurveyAnswerDO>()
                .eq(SurveyAnswerDO::getSurveyId, surveyId)
                .eq(SurveyAnswerDO::getPhone, phone)) > 0;
    }

    default Long selectCountBySurveyIdAndPhone(Long surveyId, Long phone) {
        return selectCount(new LambdaQueryWrapperX<SurveyAnswerDO>()
                .eq(SurveyAnswerDO::getSurveyId, surveyId)
                .eq(SurveyAnswerDO::getPhone, phone));
    }
}
