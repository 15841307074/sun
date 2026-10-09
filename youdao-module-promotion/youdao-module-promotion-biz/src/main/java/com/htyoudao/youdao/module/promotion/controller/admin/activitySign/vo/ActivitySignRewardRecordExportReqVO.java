package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.framework.excel.core.pojo.SearchAfterSupport;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 发放记录导出 Request VO")
@Data
public class ActivitySignRewardRecordExportReqVO implements SearchAfterSupport {

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

    @Schema(description = "发放开始时间", example = "2026-06-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime issueTimeStart;

    @Schema(description = "发放结束时间", example = "2026-06-30 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime issueTimeEnd;

    @Schema(description = "导出来源：1 PC端导出；999 手动导出。PC不传默认1，超过30万拒绝；999跳过30万限制", example = "1")
    private Integer exportSource;

    @JsonIgnore
    @Schema(hidden = true, description = "ES search_after 游标，仅导出内部使用")
    private List<FieldValue> searchAfter;

    @Override
    public void setSearchAfter(List<FieldValue> searchAfter) {
        this.searchAfter = searchAfter;
    }
}
