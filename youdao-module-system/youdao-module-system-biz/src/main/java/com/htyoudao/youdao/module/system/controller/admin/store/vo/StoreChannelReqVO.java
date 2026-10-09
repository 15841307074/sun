package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 门店分页 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreChannelReqVO {
    @Schema(description = "渠道类型")
    private String type;
    @Schema(description = "渠道ids", example = "1024")
    private List<String> channelIds = new ArrayList<>();

}
