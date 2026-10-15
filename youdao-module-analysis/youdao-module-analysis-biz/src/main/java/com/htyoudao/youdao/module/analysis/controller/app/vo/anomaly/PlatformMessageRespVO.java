package com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台消息列表返回 VO（App 接口 1）
 */
@Schema(description = "App - 平台消息")
@Data
public class PlatformMessageRespVO {

    @Schema(description = "消息ID（字符串，避免前端精度丢失）", example = "2038870253207044099")
    private String id;

    @Schema(description = "消息类型：1=周度经营异常 2=月度销量异常 3=月度0订单异常", example = "1")
    private Integer messageType;

    @Schema(description = "周期开始日", example = "2026-09-14")
    private String periodStart;

    @Schema(description = "消息正文（灰色渲染）", example = "(2026年第38周)您管辖的门店存在经营异常，")
    private String content;

    @Schema(description = "操作文案（橙色渲染）", example = "点击查看>")
    private String actionText;

    @Schema(description = "是否已读", example = "false")
    private Boolean readStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
