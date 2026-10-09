package com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 活动的兑换记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ExchangeLogRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "5732")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "会员昵称", example = "赵六")
    @ExcelProperty("会员昵称")
    private String memberNickName;

    @Schema(description = "联系电话")
    @ExcelProperty("联系电话")
    private String memberMobile;

    @Schema(description = "活动id", requiredMode = Schema.RequiredMode.REQUIRED, example = "28156")
    @ExcelProperty("活动id")
    private Long activityId;

    @Schema(description = "奖品类型 0 优惠券 1券包", example = "0")
    @ExcelProperty("奖品类型 0 优惠券 1券包")
    private Integer awardType;

    @Schema(description = "奖品名称", example = "张三")
    @ExcelProperty("奖品名称")
    private String awardName;

    @Schema(description = "奖品图片")
    @ExcelProperty("奖品图片")
    private String awardPic;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTime;

    @Schema(description = "项目标识", example = "10")
    @ExcelProperty("项目标识")
    private Long businessId;

}