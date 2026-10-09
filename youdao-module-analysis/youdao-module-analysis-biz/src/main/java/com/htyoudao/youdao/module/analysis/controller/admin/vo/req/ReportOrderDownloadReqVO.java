package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.enums.UserTypeEnum;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author DHT
 */
@Data
@Schema(description = "报表下载 - 订单下载请求入参 Request VO")
public class ReportOrderDownloadReqVO {

    /**
     * 日期选项 0 订单创建日期; 1 订单完成日期
     */
    @NotNull(message = "日期选项不能空")
    @Schema(description = "日期选项 0 订单创建日期; 1 订单完成日期")
    private Integer orderTime;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime startTime;

    /**
     * 开始时间
     */
    @Schema(description = "结束时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime endTime;

    /**
     * 是否全部门店 0 全部门店 1 指定门店
     */
    @Schema(description = "是否全部门店 0 全部门店 1 指定门店")
    @NotNull(message = "是否全部门店不能为空")
    private Integer allStore;

    /**
     * 门店 ids
     */
    @Schema(description = "门店 ids")
    private List<Long> storeIds;

    @Schema(description = "需要导出的title")
    //@InEnum(UserTypeEnum.class)
    private Set<String> fields;
}
