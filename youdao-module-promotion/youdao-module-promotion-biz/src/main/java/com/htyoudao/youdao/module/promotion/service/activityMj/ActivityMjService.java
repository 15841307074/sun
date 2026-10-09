package com.htyoudao.youdao.module.promotion.service.activityMj;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityDataAnalysisRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityNjnzSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMj.ActivityMjDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnz.ActivityNjnzDO;
import jakarta.validation.Valid;


public interface ActivityMjService extends IService<ActivityMjDO> {
    void createActivityMj(@Valid ActivityMjSaveReqVO activitySaveReqVO);

    void updateActivityMj(@Valid ActivityMjSaveReqVO activitySaveReqVO);

    void deleteActivityMj(Long id);

    ActivityMjInfoRespVO selectInfo(Long id);

}
