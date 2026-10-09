package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.framework.excel.core.annotations.DictFormat;
import com.htyoudao.youdao.framework.excel.core.convert.DictConvert;
import com.htyoudao.youdao.module.system.enums.DictTypeConstants;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 门店信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoreResVO implements Serializable {

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long storeId;

    @ExcelProperty("门店编号")
    private String storeIdStr;

    @Schema(description = "项目Id", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    private Long bussinessId;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    private String bussinessName;

    @Schema(description = "项目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    private String bussinessCode;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    @ExcelProperty("门店名称")
    private String storeName;

    @Schema(description = "经营状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer storeStatus;

    @ExcelProperty("经营状态")
    private String storeStatusName;

    @Schema(description = "营业状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer openStatus;

    @ExcelProperty("营业状态")
    private String openStatusName;

    @Schema(description = "校园配送开关 0支持 1不支持")
    private Integer campusDeliveryStatus;

    @Schema(description = "校园配送补贴")
    private java.math.BigDecimal campusDeliverySubsidy;

    @Schema(description = "门店电话", example = "15601691300")
    @ExcelProperty("门店电话")
    private String storePhone;

    @Schema(description = "组织名称 ", example = "1")
    @ExcelProperty("组织机构")
    private String orgName;

    @Schema(description = "区域经理/电话", example = "ssss")
    @DiffLogField(name = "区域经理/电话")
    private String regionUser;

    @Schema(description = "店长/电话")
    @ExcelProperty("店长/电话")
    private String storeLeader;

    @Schema(description = "门店营业时间")
    private String openTime;

    @Schema(description = "组织id ", example = "1")
    private Long orgId;

    @Schema(description = "门店营业时间")
    private String  storeHours;

    @Schema(description = "门店负责人电话 ", example = "1")
    private String storeLeaderPhone;

    @Schema(description = "标签")
    @ExcelProperty("标签")
    private String  tagName;

    @Schema(description = "加盟商姓名")
    @ExcelProperty("加盟商姓名")
    private String franchiseeName;

    @Schema(description = "加盟商手机号")
    @ExcelProperty("加盟商手机号")
    private String franchiseeMobile;

    @Schema(description = "加盟商身份证号")
    @ExcelProperty("加盟商身份证号")
    private String idCardNo;

    @Schema(description = "加盟商开户行")
    @ExcelProperty("加盟商开户行")
    private String bankName;

    @Schema(description = "加盟商开户省份")
    @ExcelProperty("加盟商开户省份")
    private String bankProvince;

    @Schema(description = "加盟商开户城市")
    @ExcelProperty("加盟商开户城市")
    private String bankCity;

    @Schema(description = "加盟商银行卡账号")
    @ExcelProperty("加盟商银行卡账号")
    private String bankCardNo;

    @Schema(description = "模板标识")
    private String identificationTemplate;
    @Schema(description = "tiktokId")
    private Long tiktokId;

    @Schema(description = "注册开关 0关 1开")
    private Integer reqState;

    @Schema(description = "推送订单开关 0关 1开")
    private Integer pushState;
}
