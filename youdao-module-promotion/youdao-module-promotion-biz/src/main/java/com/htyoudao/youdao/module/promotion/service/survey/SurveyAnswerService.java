package com.htyoudao.youdao.module.promotion.service.survey;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.FillBlankAnswerPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.FillBlankAnswerRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.SurveyAnswerPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDetailDO;

public interface SurveyAnswerService {

    /**
     * 提交答卷
     */
    Long submitAnswer(Long surveyId, Long phone, Long memberId, Integer source, String sourceIp, Integer duration, java.util.List<SurveyAnswerDetailDO> details);

    /**
     * 分页查询答卷列表
     */
    PageResult<SurveyAnswerDO> getAnswerPage(SurveyAnswerPageReqVO reqVO);

    /**
     * 获取答卷详情
     */
    SurveyAnswerDO getAnswer(Long answerId);

    /**
     * 获取答卷明细
     */
    java.util.List<SurveyAnswerDetailDO> getAnswerDetails(Long answerId);

    /**
     * 获取用户的答卷记录（按手机号）
     */
    PageResult<SurveyAnswerDO> getMyAnswerPage(Long phone, Integer pageNo, Integer pageSize);

    /**
     * 获取奖励领取结果
     */
    SurveyAnswerDO getRewardResult(Long surveyId, Long answerId);

    /**
     * 分页查询某道填空题的答案列表
     */
    PageResult<FillBlankAnswerRespVO> getFillBlankAnswerPage(FillBlankAnswerPageReqVO reqVO);

    /**
     * 导出某道填空题的答案列表（Excel）
     *
     * @param surveyId   问卷ID
     * @param questionId 题目ID
     * @param response   HTTP 响应
     */
    void exportFillBlankAnswers(Long surveyId, Long questionId, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException;
}
