package com.htyoudao.youdao.module.promotion.service.activityNjnz;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnz.ActivityNjnzDO;
import jakarta.validation.Valid;


public interface ActivityNjnzService extends IService<ActivityNjnzDO> {
    void createActivityNjnz(@Valid ActivityNjnzSaveReqVO activitySaveReqVO);

    void updateActivityNjnz(@Valid ActivityNjnzSaveReqVO activitySaveReqVO);

    void deleteActivityNjnz(Long id);

    ActivityInfoRespVO selectInfo(Long id);

    ActivityDataAnalysisRespVO getDataAnalysis(Long id);
}
