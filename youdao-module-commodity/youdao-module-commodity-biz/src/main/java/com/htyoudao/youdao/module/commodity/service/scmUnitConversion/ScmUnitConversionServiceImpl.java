package com.htyoudao.youdao.module.commodity.service.scmUnitConversion;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.commodity.dal.dataobject.ScmUnitConversion.ScmUnitConversion;
import com.htyoudao.youdao.module.commodity.dal.mysql.scmUnitConversion.ScmUnitConversionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScmUnitConversionServiceImpl implements ScmUnitConversionService {

    @Resource
    private ScmUnitConversionMapper scmUnitConversionMapper;

    @Override
    public List<ScmUnitConversion> selectByCommodityIds(List<Long> commodityIds) {

        if (ObjectUtil.isEmpty(commodityIds)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<ScmUnitConversion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ScmUnitConversion::getCommodityId, commodityIds);
        queryWrapper.eq(ScmUnitConversion::getIsDelete,0);

        return scmUnitConversionMapper.selectList(queryWrapper);
    }
}
