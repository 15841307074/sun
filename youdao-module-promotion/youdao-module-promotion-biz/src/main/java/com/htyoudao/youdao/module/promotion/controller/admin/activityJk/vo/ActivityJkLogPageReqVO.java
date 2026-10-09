package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityJkLogPageReqVO extends PageParam {


    @Schema(name = "id",description = "活动id  必传")
    @NotNull
    private Long id;

    @Schema(name = "cardType", description = "卡片类型 1 兜底卡 2 套系卡 3 万能卡 4 隐藏卡")
    private List<Integer> cardTypeList;

    @Schema(name = "memberName", description = "会员名称/手机号")
    private String memberName;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime endTime;



    @Schema(name = "storeId", description = "门店id")
    private List<Long> storeIdList;


    @Schema(name = "status", description = "使用状态（0未使用 1已使用）")
    private List<Integer> statusList;




}
