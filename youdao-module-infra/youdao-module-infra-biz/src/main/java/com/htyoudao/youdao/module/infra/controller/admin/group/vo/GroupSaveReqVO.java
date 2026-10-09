package com.htyoudao.youdao.module.infra.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - 群新增/修改 Request VO")
@Data
public class GroupSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "22512")
    private Long id;

    @Schema(description = "群名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "群名字不能为空")
    private String name;

    @Schema(description = "群成员id", example = "[4715,123]")
    private Set<Long> memberIds;

    @Schema(description = "群头像")
    private String headImage;

    @Schema(description = "群头像缩略图")
    private String headImageThumb;

    @Schema(description = "群公告")
    private String notice;

    @Schema(description = "是否被封禁 0:否 1:是")
    private Boolean isBanned;

    @Schema(description = "被封禁原因", example = "不好")
    private String reason;

    @Schema(description = "是否已解散")
    private Boolean dissolve;

}
