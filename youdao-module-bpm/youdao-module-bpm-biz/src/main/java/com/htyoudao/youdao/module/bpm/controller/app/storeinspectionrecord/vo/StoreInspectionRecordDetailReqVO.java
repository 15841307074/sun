package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@Schema(description = "app - 详情 Request VO")
public class StoreInspectionRecordDetailReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "28721")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "0 不合格、1 合格、2 不适用；不传或非法值则查全部", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2")
    private Integer actualStatus;
}
