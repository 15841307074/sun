package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 店铺关系表
 * </p>
 *
 * @author zhangjihe
 * @since 2024-05-08
 */
@Data
public class SysStoreExtendReqVO extends PageParam {

    @Serial
    private static final long serialVersionUID = -3832893305948188578L;

    /**
     * 主键
     */
    private Integer extendId;

    /**
     * 商户id
     */
    @NotNull
    private Long storeId;

    /**
     * 支付终端编号
     */
    @NotBlank
    private String terminalSn;

    /**
     * 支付secret
     */
    private String terminalKey;

    /**
     * 支付激活码
     */
    @NotBlank
    private String code;

    /**
     * 商家id
     */
//    @NotNull
    private Long businessId;
}
