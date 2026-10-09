package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Schema(description = "管理后台 - 门店信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoreLettersRespVO {

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long storeId;

    @Schema(description = "组织id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orgId;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private String storeName;

    @Schema(description = "门店是否可见 0不可见 1可见")
    private Integer visible;

    @Schema(description = "仓库id")
    private Long warehouseId;

    @Schema(description = "拼音首字母（用于字母索引）")
    private String firstLetter;

}
