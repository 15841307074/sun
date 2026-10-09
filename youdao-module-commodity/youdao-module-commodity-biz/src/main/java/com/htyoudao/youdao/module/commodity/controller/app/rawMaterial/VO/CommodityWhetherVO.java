package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import com.htyoudao.youdao.module.commodity.dal.dataobject.ScmUnitConversion.ScmUnitConversion;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CommodityWhetherVO {


    private BigDecimal totalNumber;


    private List<ScmUnitConversion> scmUnitConversions;


    //true 说明不需要替换单位  false说明需要
    private Boolean isUnit = false;
}
