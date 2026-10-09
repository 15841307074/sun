package com.htyoudao.youdao.module.infra.controller.admin.download.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class FileDownloadPageRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件名", example = "订单列表.xlsx")
    private String fileName;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "导出时间", example = "2025-01-01 23:23:23")
    private LocalDateTime createTime;

    @Schema(description = "导出人", example = "admin")
    private String creator;

    @Schema(description = "文件路径", example = "https://stage.files.htyoudao.com/excels/20250411/用户列表_20250411001439.xlsx")
    private String fileUrl;
}
