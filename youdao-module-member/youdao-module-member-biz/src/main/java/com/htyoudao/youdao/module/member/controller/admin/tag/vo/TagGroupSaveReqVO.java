package com.htyoudao.youdao.module.member.controller.admin.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 标签组新增 Request VO")
@Data
public class TagGroupSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11000")
    private Long id;

    @Schema(description = "标签名称", example = "李四")
    @Size(max = 15, message = "标签组名称最多15个字符")
    private String name;

    @Schema(description = "新增的标签体", requiredMode = Schema.RequiredMode.REQUIRED, example = "18058")
    @NotNull(message = "新增的标签体不能为空")
    private List<TagValueSaveReqVO> tagValueList;
}