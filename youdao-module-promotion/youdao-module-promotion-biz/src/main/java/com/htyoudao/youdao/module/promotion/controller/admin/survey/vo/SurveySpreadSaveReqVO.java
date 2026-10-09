package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivityChannelSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 问卷推广 新增/修改 Request VO")
@Data
public class SurveySpreadSaveReqVO {

    @Schema(description = "问卷id")
    private Long id;

    @Schema(description = "问卷类型 11-问卷调查")
    @NotNull(message = "问卷类型不能为空")
    private Integer surveyType;

    @Schema(description = "分享标题")
    @NotNull(message = "分享标题不能为空")
    private String shareTitle;

    @Schema(description = "分享内容")
    @NotNull(message = "分享描述不能为空")
    private String shareNote;

    @Schema(description = "分享图片")
    @NotNull(message = "分享图片不能为空")
    private String shareImgUrl;

    @Schema(description = "活动推广相关链接")
    @NotNull(message = "活动推广相关链接不能为空")
    @Valid
    private List<ActivityChannelSaveReqVO> activityChannelList = new ArrayList<>();
}
