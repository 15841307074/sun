package com.htyoudao.youdao.module.system.controller.admin.wxstore.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "企业微信 - 商家企业微信绑定表 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreWecomConfigRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "门店ID")
    private Long storeId;
    @Schema(description = "门店名称")
    private String storeName;
    @Schema(description = "门店店长")
    private String storeLeader;
    @Schema(description = "区域经理/电话", example = "ssss")
    @DiffLogField(name = "区域经理/电话")
    private String regionUser;
    @Schema(description = "所属组织")
    private String orgName;
    @Schema(description = "企微二维码")
    private String qrCode;

    @Schema(description = "门店经度")
    private double longitude;

    @Schema(description = "门店维度")
    private double latitude;

    @Schema(description = "店铺距离")
    private double distance;

    @Schema(description = "企微二维码类型 0福利官 1社群")
    private Integer qrType;

    @ExcelProperty("门店编号")
    private String storeIdStr;


    @Schema(description = "门店电话", example = "15601691300")
    @ExcelProperty("门店电话")
    private String storePhone;



    @Schema(description = "组织id ", example = "1")
    private Long orgId;

    @Schema(description = "企微社群二维码")
    private String qrCommunityCode;
    @Schema(description = "门店负责人电话 ", example = "1")
    private String storeLeaderPhone;
}
