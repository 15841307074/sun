package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 门店加盟商信息 Response VO
 */
@Data
@Schema(description = "管理后台 - 门店加盟商信息 Response VO")
public class StoreFranchiseeInfoRespVO {

    /**
     * 加盟商姓名
     */
    @Schema(description = "加盟商姓名")
    private String franchiseeName;

    /**
     * 加盟商手机号
     */
    @Schema(description = "加盟商手机号")
    private String franchiseeMobile;

    /**
     * 加盟商身份证号
     */
    @Schema(description = "加盟商身份证号")
    private String idCardNo;

    /**
     * 开户行
     */
    @Schema(description = "开户行")
    private String bankName;

    /**
     * 开户省份
     */
    @Schema(description = "开户省份")
    private String bankProvince;

    /**
     * 开户城市
     */
    @Schema(description = "开户城市")
    private String bankCity;

    /**
     * 银行卡账号
     */
    @Schema(description = "银行卡账号")
    private String bankCardNo;
}
