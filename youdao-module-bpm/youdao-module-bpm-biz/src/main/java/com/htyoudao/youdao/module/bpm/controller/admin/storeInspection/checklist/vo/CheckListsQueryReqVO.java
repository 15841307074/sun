package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 点检项检索 Request VO")
@Data
public class CheckListsQueryReqVO extends PageParam {

    @Schema(description = "点检项标题")
    private String title;

    @Schema(description = "大类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long typeId;

}
