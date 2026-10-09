package com.htyoudao.youdao.module.system.controller.admin.wxstore.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoresBatchVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 门店批量修改二维码类型 Response VO")
@Data
public class StoresCodeUpdateVO {
    @Schema(description = "门店ids", requiredMode = Schema.RequiredMode.REQUIRED)
    private  List<Long> storeIds = new ArrayList<>();
    @Schema(description = "code类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private int codeType;
}
