package com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史过期用户券归档请求。
 */
@Data
@Schema(description = "管理后台 - 历史过期用户券归档请求")
public class UserCouponExpiredArchiveReqVO {

    @NotEmpty(message = "归档表名不能为空")
    @Schema(description = "待处理物理表，只允许 user_coupon_0 至 user_coupon_9",
            example = "[\"user_coupon_1\"]")
    private List<String> tableNames;

    @NotNull(message = "过期截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "归档 expiration_time 小于等于该时间的记录",
            example = "2026-01-01 00:00:00")
    private LocalDateTime expirationTime;

    @Schema(description = "单批数量，默认2000，范围100-5000", example = "2000")
    private Integer batchSize;

    @Schema(description = "分表并发数，默认2，范围1-3；同一张表内部始终串行", example = "2")
    private Integer concurrency;

    @Schema(description = "每批提交后的休眠毫秒数，默认100，范围0-1000", example = "100")
    private Long batchIntervalMillis;
}
