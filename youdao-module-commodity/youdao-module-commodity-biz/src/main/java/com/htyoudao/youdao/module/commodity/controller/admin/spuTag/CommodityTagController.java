package com.htyoudao.youdao.module.commodity.controller.admin.spuTag;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;

import com.htyoudao.youdao.module.commodity.controller.admin.spuTag.VO.CommodityTagReqVO;
import com.htyoudao.youdao.module.commodity.service.spuTag.ICommodityTageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 商品标签")
@RestController
@RequestMapping("/commodity/commodity-tag")
public class CommodityTagController {

    @Resource
    private ICommodityTageService commodityTageService;

    @Operation(summary = "新增商品标签")
    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('commodity:tag:create')")
    public CommonResult<Boolean> create(@Valid @RequestBody CommodityTagReqVO commodityTagReqVO) {
        commodityTageService.create(commodityTagReqVO);
        return success(true);
    }

    @Operation(summary = "修改商品标签")
    @PostMapping("/update")
    @PreAuthorize("@ss.hasPermission('commodity:tag:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody CommodityTagReqVO commodityTagReqVO) {
        commodityTageService.update(commodityTagReqVO);
        return success(true);
    }

    @Operation(summary = "获取商品标签")
    @GetMapping("/getById/{id}")
    @PreAuthorize("@ss.hasPermission('commodity:tag:query')")
    public CommonResult<CommodityTagReqVO> getById(@PathVariable("id") Long id) {
        CommodityTagReqVO commodityTagReqVO =  commodityTageService.getById(id);
        return success(commodityTagReqVO);
    }
    @Operation(summary = "商品标签列表")
    @PostMapping("/selectList")
    @PreAuthorize("@ss.hasPermission('commodity:tag:query')")
    public CommonResult<List<CommodityTagReqVO>> selectList(@RequestBody CommodityTagReqVO commodityTagReqVO) {
        List<CommodityTagReqVO> commodityTagReqVOList =   commodityTageService.selectList(commodityTagReqVO);
        return success(commodityTagReqVOList);
    }

    @Operation(summary = "删除商品标签")
    @GetMapping("/delById/{id}")
    @PreAuthorize("@ss.hasPermission('commodity:tag:delete')")
    public CommonResult<Boolean> delById(@PathVariable("id") Long id) {
         commodityTageService.delById(id);
        return success(true);
    }
}
