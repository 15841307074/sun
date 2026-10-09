package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class ActivityCqReqVO {

    /**
     * 子表id
     */

    @Schema(description = "子表id")
    private Long id;

    /**
     * 活动id
     */

    @Schema(description = "活动id")
    private Long activityId;

    /**
     * 活动名称
     */

    @Schema(description = "活动名称")
    private String activityName;

    /**
     * 是否启用
     */

    @Schema(description = "是否启用")
    private Integer isEnabled;

    /**
     * 活动封面图
     */

    @Schema(description = "活动封面图")
    private String activityCoverImage;

    /**
     * 结果公布时间
     */

    @Schema(description = "结果公布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date resultPublishTime;

    /**
     * 公开展示
     */

    @Schema(description = "公开展示")
    private Integer publicButton;

    /**
     * 开奖状态
     */
    @Schema(description = "开奖状态")
    private Integer drawStatus;
}
