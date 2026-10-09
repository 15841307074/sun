package com.htyoudao.youdao.module.system.controller.app.printer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 该类表示通用的信息，包含文字大小和字体是否加粗的信息。
 */
@Data
public class InformationVO {
    @Schema(description = "文字大小 0 大  1 中  2 小。", example = "1024")
    private Integer textSize = 1;
    @Schema(description = "字体是否加粗", example = "1024")
    private Boolean fontBold = false;
}