package com.htyoudao.youdao.module.system.controller.admin.business.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSimpleResVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusinessStoreRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7398")
    @ExcelProperty("ID")
    private Long id;
    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("项目编号")
    private String businessName;
    @Schema(description = "门店信息")
    private List<StoreSimpleResVO> storeInfoVOList;
}