package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 点检项 Response VO")
@Data
public class CheckListRespVO extends PageParam {

    @Schema(description = "主键ID")
    private Long checklistId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String title;

    @Schema(description = "大类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String typeId;

    @Schema(description = "大类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大类")
    private String typeName;

    @Schema(description = "提示", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String prompt;

    @Schema(description = "总分", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Integer score;

    @Schema(description = "0 不允许上传 1 允许上传", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer imgFlag;

    @Schema(description = "0 不必传 1 合格时必传 2 不合格时必传 3 不适用时必传", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private String imgState;

    @Schema(description = "上传图片个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer imgCount;

    @Schema(description = "0 不需要整改人 1 需要整改人", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Integer rectifierFlag;

    @Schema(description = "整改人")
    private String rectifier;

    @Schema(description = "0 不需要审核人 1 需要审核人")
    private Integer approverFlag;

    @Schema(description = "审核人")
    private String approver;

    @Schema(description = "抄送人")
    private String ccPerson;

    @Schema(description = "整改有效期")
    private Integer deadline;

    @Schema(description = "申述有效期")
    private Integer validityPeriod;

    @Schema(description = "0 不需要上传整改图片 1 需要上传整改图片")
    private Integer rectifyImgFlag;

    @Schema(description = "0 不允许实时拍照 1允许实时拍照")
    private Integer realFlag;

    @Schema(description = "点检标准")
    private String inspectionStandard;

    @Schema(description = "排序")
    private Integer sort;

}
