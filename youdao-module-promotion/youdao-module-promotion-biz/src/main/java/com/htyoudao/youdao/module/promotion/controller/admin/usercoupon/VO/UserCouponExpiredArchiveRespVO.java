package com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 历史过期用户券归档结果。
 */
@Data
@AllArgsConstructor
@Schema(description = "管理后台 - 历史过期用户券归档结果")
public class UserCouponExpiredArchiveRespVO {

    @Schema(description = "各物理表成功备份并删除的数量")
    private Map<String, Long> archivedCounts;

    @Schema(description = "本次总归档数量")
    private long totalArchivedCount;

    @Schema(description = "总耗时（毫秒）")
    private long costMillis;
}
