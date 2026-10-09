package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RawMaterialIdVO {

    private Long rawMaterialId;

    private BigDecimal inventory;
}
