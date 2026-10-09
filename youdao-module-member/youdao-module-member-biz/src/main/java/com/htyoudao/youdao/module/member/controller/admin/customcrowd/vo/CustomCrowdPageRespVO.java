package com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 自定义人群分页 Request VO")
@Data
public class CustomCrowdPageRespVO {

    @Schema(description = "人群id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "人群名称")
    private String crowdName;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;
}
