package com.htyoudao.youdao.module.system.controller.admin.wxstore.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Schema(description = "管理后台 - 门店分页 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StoreWecomPageReqVO extends PageParam {
    @Schema(description = "组织id", example = "1024")
    private Long orgId;
    @Schema(description = "门店名称/门店编码，模糊匹配", example = "youdao")
    private String text;
    @Schema(description = "是否上传 0 是  1 否", example = "1024")
    private Integer isUpload;
    private Set<Long> orgIds;
    @Schema(description = "标签id", example = "1024")
    private Set<Long> tagId;
}
