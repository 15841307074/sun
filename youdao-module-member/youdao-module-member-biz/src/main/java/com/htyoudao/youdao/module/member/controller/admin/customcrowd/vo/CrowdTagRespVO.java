package com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 自定义人群 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrowdTagRespVO {

    @Schema(description = "标签名称", required = true, example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private String tagName;

    @Schema(description = "标签id", required = true, example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tagId;

    @Schema(description = "人群id", required = true, example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long crowdId;
}
