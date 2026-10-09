package com.htyoudao.youdao.module.promotion.service.survey;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.survey.vo.AppSurveyShareVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyDO;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;

public interface SurveyService {

    /**
     * 创建问卷
     */
    Long createSurvey(SurveySaveReqVO reqVO);

    /**
     * 更新问卷
     */
    void updateSurvey(SurveySaveReqVO reqVO);

    /**
     * 删除问卷
     */
    void deleteSurvey(Long id);

    /**
     * 复制问卷
     */
    Long copySurvey(Long id);

    /**
     * 发布/停用问卷（status=1发布，status=2停用）
     * 发布时：若前端传了startTime则用前端的，否则若未设置开始时间则自动设为当前时间
     * 停用时：若前端传了endTime则用前端的，否则自动设为当前时间
     */
    void updateStatus(Long id, Integer status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取问卷详情
     */
    SurveyDO getSurvey(Long id);

    /**
     * 获取问卷详情（含题目和选项）
     */
    SurveyDetailRespVO getSurveyDetail(Long id);

    /**
     * 分页查询问卷列表
     */
    PageResult<SurveyDO> getSurveyPage(SurveyPageReqVO reqVO);

    /**
     * 查询问卷活动的推广
     */
    SurveySpreadRespVO selectSpread(Long id);

    /**
     * 修改问卷活动的推广
     */
    void updateSpread(SurveySpreadSaveReqVO reqVO);

    /**
     * 获取题目统计
     */
    SurveyStatsRespVO getStats(Long id);

    /**
     * 导出问卷答卷数据
     */
    void exportAnswers(Long id, HttpServletResponse response);

    /**
     * 获取问卷分享详情
     */
    AppSurveyShareVO getShareVO(Long id);
}
