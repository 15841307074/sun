package com.htyoudao.youdao.module.member.controller.admin.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 标签组修改 Request VO")
@Data
public class TagGroupUpdateReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11000")
    private Long id;

    @Schema(description = "标签名称", example = "李四")
    private String name;

    @Schema(description = "新增的标签体", requiredMode = Schema.RequiredMode.REQUIRED, example = "18058")
    private List<TagValueSaveReqVO> tagValueList;

    @Schema(description = "要删除的标签ids", requiredMode = Schema.RequiredMode.REQUIRED, example = "18058")
    private List<Long> tagValueIdList;

    @Schema(description = "修改的标签体", requiredMode = Schema.RequiredMode.REQUIRED, example = "18058")
    private List<TagValueSaveReqVO> updateValueList;
}
