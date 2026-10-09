package com.htyoudao.youdao.module.commodity.service.scmUnitConversion;

import com.htyoudao.youdao.module.commodity.dal.dataobject.ScmUnitConversion.ScmUnitConversion;

import java.util.List;

public interface ScmUnitConversionService {
    List<ScmUnitConversion> selectByCommodityIds(List<Long> commodityIds);
}
