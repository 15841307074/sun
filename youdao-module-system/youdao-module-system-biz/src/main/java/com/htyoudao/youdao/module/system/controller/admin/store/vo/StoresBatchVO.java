package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 门店批量添加/删除标签 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoresBatchVO {

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long storeId;
    @Schema(description = "标签")
    private List<Long>  tagIds;
}
