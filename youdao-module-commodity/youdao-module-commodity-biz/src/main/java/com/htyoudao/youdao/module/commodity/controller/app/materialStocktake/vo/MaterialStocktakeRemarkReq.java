package com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaterialStocktakeRemarkReq {

    @NotNull
    private Long id;

    @NotBlank
    private String remark;
}
