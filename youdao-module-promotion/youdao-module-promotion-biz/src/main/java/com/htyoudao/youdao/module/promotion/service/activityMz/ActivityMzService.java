package com.htyoudao.youdao.module.promotion.service.activityMz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo.ActivityMzInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo.ActivityMzSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMz.ActivityMzDO;
import jakarta.validation.Valid;

/**
 * 满赠活动 Service 接口
 */
public interface ActivityMzService extends IService<ActivityMzDO> {

    void createActivityMz(@Valid ActivityMzSaveReqVO activitySaveReqVO);

    void updateActivityMz(@Valid ActivityMzSaveReqVO activitySaveReqVO);

    void deleteActivityMz(Long id);

    ActivityMzInfoRespVO selectInfo(Long id);
}
