package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class ActivityCqLogExportReqVO {

    /**
     * 活动id
     */
    @Schema(description = "活动id")
    @NotNull
    private Long id;

    /**
     * 会员昵称/手机号/签码
     */
    @Schema(description = "会员昵称/手机号")
    private String memberName;

    /**
     * 获取方式
     */
    @Schema(description = "获取方式")
    private List<Integer> obtainTypeList;

    /**
     * 奖品类型
     */
    @Schema(description = "奖品类型")
    private List<Integer> prizeTypeList;

    /**
     * 奖品类型
     */
    @Schema(description = "奖品类型")
    private Integer prizeType;

    /**
     * 结果状态
     */
    @Schema(description = "结果状态")
    private List<Integer> resultStatusList;

    /**
     * 红包状态
     */
    @Schema(description = "红包状态")
    private List<Integer> redPacketStatusList;

    /**
     * 红包状态
     */
    @Schema(description = "红包状态")
    private Integer claimStatus;

    /**
     * 奖品状态
     */
    @Schema(description = "奖品状态")
    private List<Integer> prizeStateList;

    /**
     * 门店id
     */
    @Schema(description = "门店id")
    private List<Long> storeIdList;

    /**
     * 抽签门店id
     */
    @Schema(description = "抽签门店id")
    private Long storeId;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, fallbackPatterns = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, fallbackPatterns = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    /**
     * 抽签时间范围
     */
    @Schema(description = "抽签时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, fallbackPatterns = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(contentUsing = LocalDateTimeSerializer.class)
    @JsonDeserialize(contentUsing = LocalDateTimeDeserializer.class)
    private List<LocalDateTime> activityTime;

    /**
     * 收货地址状态 0全部 1未填写 2已填写
     */
    @Schema(description = "收货地址状态 0全部 1未填写 2已填写")
    private Integer receiveAddressType;

    /**
     * 物流单号状态 0全部 1未填写 2已填写
     */
    @Schema(description = "物流单号状态 0全部 1未填写 2已填写")
    private Integer trackingType;
}
