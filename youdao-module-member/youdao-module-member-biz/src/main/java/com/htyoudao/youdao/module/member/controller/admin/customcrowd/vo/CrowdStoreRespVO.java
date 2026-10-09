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
public class CrowdStoreRespVO {

    @Schema(description = "门店id", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long storeId;

    @Schema(description = "门店名称", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private String storeName;

    @Schema(description = "人群id", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long crowdId;
}
