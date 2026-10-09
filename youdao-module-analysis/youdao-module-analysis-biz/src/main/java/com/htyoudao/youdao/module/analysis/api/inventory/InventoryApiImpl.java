package com.htyoudao.youdao.module.analysis.api.inventory;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.InventoryAggregationRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.service.IStoreAggService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * @author dht
 */
@DubboService(timeout = 50000)
@Slf4j
public class InventoryApiImpl implements InventoryApi{

    @Resource
    private IStoreAggService aggregationService;


    @Override
    public CommonResult<BigDecimal> storePage(InventoryAggregationRequest requestVO) {

        return CommonResult.success(aggregationService.storeTotalAmount(requestVO));
    }
}
