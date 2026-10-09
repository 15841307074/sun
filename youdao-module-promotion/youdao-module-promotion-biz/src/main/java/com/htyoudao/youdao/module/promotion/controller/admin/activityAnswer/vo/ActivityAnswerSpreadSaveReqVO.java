package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivityChannelSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "有奖问答活动推广修改请求")
public class ActivityAnswerSpreadSaveReqVO {

    @Schema(description = "活动 ID")
    @NotNull(message = "活动 ID 不能为空")
    private Long id;

    @Schema(description = "分享标题")
    @NotBlank(message = "分享标题不能为空")
    private String shareTitle;

    @Schema(description = "分享内容")
    @NotBlank(message = "分享内容不能为空")
    private String shareNote;

    @Schema(description = "分享图片")
    @NotBlank(message = "分享图片不能为空")
    private String shareImgUrl;

    @Valid
    @Schema(description = "活动推广相关链接")
    private List<ActivityChannelSaveReqVO> activityChannelList = new ArrayList<>();
}
