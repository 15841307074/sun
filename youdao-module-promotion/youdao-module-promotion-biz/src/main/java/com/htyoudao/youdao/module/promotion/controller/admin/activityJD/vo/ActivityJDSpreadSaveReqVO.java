package com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo;

import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivityChannelSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Schema(description = "集点活动推广页 新增/修改 ")
@Data
public class ActivityJDSpreadSaveReqVO {

    @Schema(description = "活动 id")
    private Long id;

    // 分享图片
    @Schema(description = "分享图片")
    @NotNull(message = "分享图片 不能为空")
    private String shareImageUrl;

    // 分享标题
    @Schema(description = "分享标题")
    @NotNull(message = "分享标题 不能为空")
    private String shareTitle;

    // 分享描述
    @Schema(description = "分享描述")
    @NotNull(message = "分享描述 不能为空")
    private String shareDescription;

    @Schema(description = "活动推广相关链接")
    @NotNull(message = "活动推广相关链接 不能为空")
    @Valid
    private List<ActivityChannelSaveReqVO> activityChannelList =new ArrayList<>();
}
