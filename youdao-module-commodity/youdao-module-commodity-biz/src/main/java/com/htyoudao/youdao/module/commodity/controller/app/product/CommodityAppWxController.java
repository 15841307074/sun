package com.htyoudao.youdao.module.commodity.controller.app.product;


import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.ActivitySeckillCommodityRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.ActivitySeckillRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.ActivitySeckillTimeRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.AppletSpuDetailReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.SeckillSpuVO;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import com.htyoudao.youdao.module.commodity.dal.redis.CommodityStoreRedisDao;
import com.htyoudao.youdao.module.commodity.enums.ClientType;
import com.htyoudao.youdao.module.commodity.service.activity.SeckillActivityService;
import com.htyoudao.youdao.module.commodity.service.storeSpu.ICommodityStoreSpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "app - 小程序门店商品接口")
@RestController
@RequestMapping("/commodity/app/wx")
@Validated
@Slf4j
public class CommodityAppWxController {

    @Resource
    private ICommodityStoreSpuService commodityStoreSpuService;

    @Resource
    private CommodityStoreRedisDao storeRedisDao;

    @Resource
    private SeckillActivityService seckillActivityService;


    @GetMapping("/appletGetAllSpu")
    @Operation(summary = "小程序获取门店分类商品接口")
    @PermitAll
    public CommonResult<List<StoreCategoryDTO>> wxAppletGetAllSpu(@Param("storeId") Long storeId) {
        return success(commodityStoreSpuService.appletGetAllSpu(storeId, ClientType.WX));
    }


    @GetMapping("/appletSpuDetail")
    @Operation(summary = "小程序获取门店商品详情")
    @PermitAll
    public CommonResult<SpuDto> appletSpuDetail(@Valid AppletSpuDetailReqVO reqVO) {
        Long categoryId = reqVO.getCategoryId();
        Long spuId = reqVO.getSpuId();
        Long storeId = reqVO.getStoreId();
        return success(storeRedisDao.getSpuDetail(storeId, categoryId, spuId));
    }

    @GetMapping("/seckill/list")
    @Operation(summary = "小程序秒杀商品列表")
    @PermitAll
    public CommonResult<List<SeckillSpuVO>> seckillList(Long storeId, Long activityId, Integer times) {
        List<SeckillSpuVO> list = seckillActivityService.seckillList(storeId, activityId, times);
        return success(list);
    }
}
