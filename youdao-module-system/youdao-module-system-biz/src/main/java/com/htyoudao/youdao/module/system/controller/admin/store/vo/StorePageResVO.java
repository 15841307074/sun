package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - 门店信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StorePageResVO implements Serializable {

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long storeId;

    @ExcelProperty("门店地址")
    private String storeAddress;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    @ExcelProperty("门店名称")
    private String storeName;

    @Schema(description = "组织名称 ", example = "1")
    @ExcelProperty("组织名称")
    private String orgName;

    @Schema(description = "店长/电话")
    private String storeLeader;





    @Schema(description = "经营状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer storeStatus;
    @Schema(description = "营业状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer openStatus;
    @Schema(description = "门店电话", example = "15601691300")
    @ExcelProperty("门店电话")
    private String storePhone;

    @Schema(description = "区域经理/电话", example = "ssss")
    @DiffLogField(name = "区域经理/电话")
    private String regionUser;

    @Schema(description = "门店营业时间")
    private String openTime;
    @Schema(description = "组织id ", example = "1")
    private Long orgId;
    @Schema(description = "门店营业时间")
    private String  storeHours;
    @Schema(description = "门店负责人电话 ", example = "1")
    private String storeLeaderPhone;
    @Schema(description = "标签")
    private String  tagName;



}
