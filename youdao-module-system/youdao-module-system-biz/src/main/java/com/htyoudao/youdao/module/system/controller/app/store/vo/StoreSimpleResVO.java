package com.htyoudao.youdao.module.system.controller.app.store.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 门店信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoreSimpleResVO {

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("门店id")
    private Long storeId;
    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    @ExcelProperty("门店名称")
    private String storeName;
}
