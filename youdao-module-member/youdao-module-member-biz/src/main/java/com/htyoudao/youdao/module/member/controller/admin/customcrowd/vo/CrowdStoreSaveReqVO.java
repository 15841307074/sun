package com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 自定义人群新增/修改 Request VO")
@Data
public class CrowdStoreSaveReqVO {

    @Schema(description = "门店id", required = true, example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long storeId;

    private String storeName;

    @Schema(description = "人群id", required = true, example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long crowdId;
}
