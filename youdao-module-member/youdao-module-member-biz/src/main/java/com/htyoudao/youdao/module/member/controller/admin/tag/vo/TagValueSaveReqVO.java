package com.htyoudao.youdao.module.member.controller.admin.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 标签新增/修改 Request VO")
@Data
public class TagValueSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2554")
    private Long id;

    @Schema(description = "标签名称", example = "张三")
    @Size(max = 15, message = "标签组名称最多15个字符")
    private String name;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "标签组id", example = "3530")
    private Long tagGroupId;

    /**
     * 项目 id
     */
    private Long businessId;
}