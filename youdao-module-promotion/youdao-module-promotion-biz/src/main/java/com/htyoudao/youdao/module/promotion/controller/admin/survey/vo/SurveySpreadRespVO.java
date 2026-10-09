package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "管理后台 - 问卷推广详情 Response VO")
public class SurveySpreadRespVO {

    @Schema(description = "问卷id")
    private Long id;

    @Schema(description = "问卷类型 12-问卷调查")
    private Integer surveyType;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "活动推广渠道链接集合")
    private List<ActivityChannelRespVO> activityChannelRespVOS = new ArrayList<>();
}
