package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.framework.common.util.validation.ValidationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 管理后台 - 门店加盟商信息保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 门店加盟商信息保存 Request VO")
public class StoreFranchiseeInfoSaveReqVO {

    /**
     * 加盟商姓名
     */
    @Schema(description = "加盟商姓名", example = "张三")
    @Length(max = 64, message = "加盟商姓名长度不能超过64个字符")
    private String franchiseeName;

    /**
     * 加盟商手机号
     */
    @Schema(description = "加盟商手机号", example = "13800138000")
    @Length(max = 20, message = "加盟商手机号长度不能超过20个字符")
    private String franchiseeMobile;

    /**
     * 加盟商身份证号
     */
    @Schema(description = "加盟商身份证号")
    @Length(max = 32, message = "加盟商身份证号长度不能超过32个字符")
    private String idCardNo;

    /**
     * 开户行
     */
    @Schema(description = "开户行", example = "中国工商银行")
    @Length(max = 128, message = "开户行长度不能超过128个字符")
    private String bankName;

    /**
     * 开户省份
     */
    @Schema(description = "开户省份", example = "辽宁省")
    @Length(max = 64, message = "开户省份长度不能超过64个字符")
    private String bankProvince;

    /**
     * 开户城市
     */
    @Schema(description = "开户城市", example = "沈阳市")
    @Length(max = 64, message = "开户城市长度不能超过64个字符")
    private String bankCity;

    /**
     * 银行卡账号
     */
    @Schema(description = "银行卡账号")
    @Length(max = 64, message = "银行卡账号长度不能超过64个字符")
    @Pattern(regexp = "^(?:\\s*|(?:\\d\\s*){8,32})$", message = "银行卡账号必须为8到32位数字")
    private String bankCardNo;

    /**
     * 身份证为空时不校验，有值时使用标准身份证规则校验。
     */
    @JsonIgnore
    @AssertTrue(message = "加盟商身份证号格式不正确")
    public boolean isIdCardNoValid() {
        return StrUtil.isBlank(idCardNo) || IdcardUtil.isValidCard(idCardNo.trim());
    }

    /**
     * 手机号为空时不校验，有值时沿用项目统一的大陆手机号规则。
     */
    @JsonIgnore
    @AssertTrue(message = "加盟商手机号格式不正确")
    public boolean isFranchiseeMobileValid() {
        return StrUtil.isBlank(franchiseeMobile) || ValidationUtils.isMobile(franchiseeMobile.trim());
    }
}
