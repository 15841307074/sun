package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerQuestionDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRewardDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 有奖问答聚合缓存数据，方便小程序端按一次缓存读取活动、门店、商品、题目和奖品配置。
 */
@Data
public class ActivityAnswerSettingsCacheDataVO {

    @Schema(description = "活动主表ID")
    private Long activityId;

    @Schema(description = "有奖问答配置")
    private ActivityAnswerDO answer;

    @Schema(description = "活动主表配置")
    private ActivityDO activity;

    @Schema(description = "可参与门店ID集合")
    private List<Long> storeIds = Collections.emptyList();

    @Schema(description = "下单任务指定商品ID集合")
    private List<Long> commodityIds = Collections.emptyList();

    @Schema(description = "题目列表")
    private List<ActivityAnswerQuestionDO> questions = Collections.emptyList();

    @Schema(description = "奖品档位列表")
    private List<ActivityAnswerRewardDO> rewards = Collections.emptyList();
}
