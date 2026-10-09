package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 门店批量添加/删除标签 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoresUpdateVO {
    @Schema(description = "门店批量标签", requiredMode = Schema.RequiredMode.REQUIRED)
   List<StoresBatchVO> storesBatchVOList = new ArrayList<>();
}
