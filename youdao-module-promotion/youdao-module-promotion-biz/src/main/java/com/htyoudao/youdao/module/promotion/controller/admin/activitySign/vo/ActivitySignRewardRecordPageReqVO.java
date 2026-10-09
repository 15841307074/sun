package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 发放记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivitySignRewardRecordPageReqVO extends PageParam {

    @Schema(description = "活动ID，当前活动详情页必传", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "手机号", example = "13800138000")
    private String memberMobile;

    @Schema(description = "奖励类型，复用ActivityCqPrizeTypeEnum：1优惠券 2积分 3实物 5现金红包 6优惠券包", example = "2")
    private Integer prizeType;

    @Schema(description = "红包领取状态，多选：1未领取 2已领取 3已过期")
    private List<Integer> claimStatusList;

    @Schema(description = "收货地址状态：1未填写 2已填写", example = "1")
    private Integer receiveAddressStatus;

    @Schema(description = "物流单号状态：1未填写 2已填写", example = "1")
    private Integer trackingNumberStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "发放开始时间", example = "2026-06-01 00:00:00")
    private LocalDateTime issueTimeStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "发放结束时间", example = "2026-06-30 23:59:59")
    private LocalDateTime issueTimeEnd;
    @Schema(hidden = true, description = "后端调试参数：true 时跳过中间件，直接查询 MySQL")
    private Boolean forceMysql;

}
