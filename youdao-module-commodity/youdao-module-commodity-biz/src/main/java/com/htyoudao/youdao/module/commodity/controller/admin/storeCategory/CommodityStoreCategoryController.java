package com.htyoudao.youdao.module.commodity.controller.admin.storeCategory;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.controller.admin.storeCategory.VO.CategoryStoreSortVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeCategory.VO.CommodityStoreCategoryRespVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreCategory;
import com.htyoudao.youdao.module.commodity.service.storeCategory.ICommodityStoreCategoryService;
import com.htyoudao.youdao.module.commodity.service.storeSpu.ICommodityStoreSpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


/**
 * 门店下商品分类表 前端控制器
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Tag(name = "管理后台 - 门店下商品分类表")
@RestController
@RequestMapping("/commodity/commodity-store-category")
public class CommodityStoreCategoryController {

    @Autowired
    private ICommodityStoreCategoryService iCommodityStoreCategoryService;

    @Autowired
    private ICommodityStoreSpuService iCommodityStoreSpuService;


    @Operation(summary = "分类详情")
    @PostMapping("/getCategory")
    @PreAuthorize("@ss.hasPermission('commodity:store:query')")
    public CommonResult<CommodityStoreCategory> getCategoryV3(@RequestParam Long commodityStoreCategoryId) {
        CommodityStoreCategory byId = iCommodityStoreCategoryService.getCategoryById(commodityStoreCategoryId);

        return success(byId);
    }
    @Operation(summary = "获取分类列表")
    @GetMapping("/getList")
    @PreAuthorize("@ss.hasPermission('commodity:store:query')")
    public CommonResult<List<CommodityStoreCategoryRespVO>> getList(@RequestParam Long storeId){
        List<CommodityStoreCategoryRespVO> commodityStoreCategoryRespVOList = iCommodityStoreCategoryService.getList(storeId);
        return success(commodityStoreCategoryRespVOList);
    }

    /**
     * 门店分类拖动排序
     */
    @PostMapping("/sortStoreCategory")
    @Operation(summary = "门店分类拖动排序")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    public CommonResult<Boolean> sortStoreCategory(@RequestBody @Valid List<CategoryStoreSortVO> categorySortDTOList) {
        iCommodityStoreCategoryService.sortCategory(categorySortDTOList);
        return success(true);
    }


    @Operation(summary = "获取分类列表")
    @GetMapping("/getListTwo")
    public CommonResult<List<CommodityStoreCategoryRespVO>> getListTwo(@RequestParam Long storeId){
        List<CommodityStoreCategoryRespVO> commodityStoreCategoryRespVOList = iCommodityStoreCategoryService.getListTwo(storeId);
        return success(commodityStoreCategoryRespVOList);
    }



}
