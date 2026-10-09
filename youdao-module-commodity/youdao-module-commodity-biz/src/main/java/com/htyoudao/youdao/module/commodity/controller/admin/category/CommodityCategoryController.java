package com.htyoudao.youdao.module.commodity.controller.admin.category;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryBatchHiddenReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategorySortDTO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCategory;
import com.htyoudao.youdao.module.commodity.service.category.ICommodityCategoryService;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryIdReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategorySaveReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategorySortReqVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCategory;
import com.htyoudao.youdao.module.commodity.service.category.ICommodityCategoryService;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 商品分类")
@RestController
@RequestMapping("/commodity/category")
public class CommodityCategoryController {
    @Resource
    private ICommodityCategoryService iCommodityCategoryService;

    @Resource
    private ICommoditySpusService commoditySpusService;




    @Operation(summary = "订单模块在使用")
    @GetMapping("/getCommodityCategoryList")
    CommonResult<List<CategoryRespVo>> getCommodityCategoryList() {
        return success(
                iCommodityCategoryService.getCommodityCategoryList()
        );
    }


    @Operation(summary = "商品分组新建接口(PC)")
    @PostMapping("/V3/createCategory")
    @PreAuthorize("@ss.hasPermission('commodity:spu:create')")
    public CommonResult<Long> createCategoryV3(@Valid @RequestBody CategorySaveReqVo saveReqVo) {
        saveReqVo.setName(saveReqVo.getName().trim());
        Long id = iCommodityCategoryService.createCategoryV3(saveReqVo);
        return success(id);
    }

    @Operation(summary = "获取分类详情")
    @PostMapping("/V3/infoCategory")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<CategoryRespVo> infoCategory(@Valid @RequestBody CategoryIdReqVo idReqVo) {
        CategoryRespVo category = iCommodityCategoryService.getCategoryInfo(idReqVo);
        return success(category);
    }


    @Operation(summary = "分组拖动排序")
    @PostMapping("/V3/sortCategory")
    @PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<Boolean> sortCategory(@Valid @RequestBody List<CategorySortReqVo> categorySortReqVoList) {
        iCommodityCategoryService.sortCategory(categorySortReqVoList);
        return success(true);
    }

    @Operation(summary = "批量隐藏或恢复商品分类")
    @PostMapping("/V3/batchUpdateHidden")
    @PreAuthorize("@ss.hasPermission('commodity:cate:update')")
    public CommonResult<Boolean> batchUpdateHidden(@Valid @RequestBody CategoryBatchHiddenReqVO reqVO) {
        commoditySpusService.batchUpdateCategoryHidden(reqVO);
        return success(true);
    }



}
