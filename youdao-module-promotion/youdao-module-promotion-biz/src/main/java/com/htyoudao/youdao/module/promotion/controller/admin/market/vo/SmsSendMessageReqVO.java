package com.htyoudao.youdao.module.promotion.controller.admin.market.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author dht
 */
@Data
@Schema(name = "营销短信发送2", description = "营销短信发送2")
@ToString(callSuper = true)
public class SmsSendMessageReqVO {


    @Schema(description = "短信营销id")
    private Long id;

    @Schema(description = "电话号")
    private String mobile;

    /**
     * 手机号集合 后端测试用
     */
    @Schema(description = "手机号集合 后端测试用")
    private List<String> mobiles;

    /**
     * 发送方式 0 立即发送 1定时发送
     */
    @Schema(description = "发送方式 0 立即发送 1定时发送")
    private Integer sendMethod;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime sendTime;
}
