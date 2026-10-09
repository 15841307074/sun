package com.htyoudao.youdao.module.infra.controller.admin.download.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-15
 */
@Schema(description = "管理后台 - 任务下载列表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class FileDownloadPageReqVO extends PageParam {

    @Serial
    private static final long serialVersionUID = -572707883799465942L;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
