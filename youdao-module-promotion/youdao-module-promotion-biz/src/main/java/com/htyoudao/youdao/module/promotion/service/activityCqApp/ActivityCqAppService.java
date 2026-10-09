package com.htyoudao.youdao.module.promotion.service.activityCqApp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqDetailVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqPrizeListVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.AddressSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.DrawRecordVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.MyCodeVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.MyResultVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.TaskVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.WinningRecordVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqDO;

import java.util.List;

public interface ActivityCqAppService extends IService<ActivityCqDO> {

    ActivityCqDetailVO getActivityCqDetail(Long activityId, Long storeId, Long memberId);

    List<TaskVO> getTaskList(ActivityCqReqVO reqVO);

    Boolean join(ActivityCqReqVO reqVO);

    void validateJoined(Long activityId, Long memberId);

    Boolean orderTask(ActivityCqReqVO reqVO);

    List<String> signTask(ActivityCqReqVO reqVO);

    List<String> simpleShareTask(ActivityCqReqVO reqVO);

    List<String> browseHomeTask(ActivityCqReqVO reqVO);

    Boolean shareTask(ActivityCqReqVO reqVO);

    Boolean shareCheck(ActivityCqReqVO reqVO);

    ActivityCqPrizeListVO getPrizeList(ActivityCqReqVO reqVO);

    Boolean saveAddress(AddressSaveReqVO reqVO);

    PageResult<DrawRecordVO> getDrawRecord(ActivityCqReqVO reqVO);

    MyCodeVO getMyCodeInfo(ActivityCqReqVO reqVO);

    PageResult<MyResultVO> getMyResultRecord(ActivityCqReqVO reqVO);

    PageResult<WinningRecordVO> getWinningRecord(ActivityCqReqVO reqVO);

    List<String> createCodeLogs(Long activityId, Long memberId, Long storeId, Integer obtainType, int count);
}
