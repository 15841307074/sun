package com.htyoudao.youdao.module.commodity.controller.admin.triRecipe;

import cn.hutool.core.collection.CollectionUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriFormulationDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriRecipeDO;
import com.htyoudao.youdao.module.commodity.service.triInventory.ITriRecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


/**
 * 商品配方Controller
 *
 * @author lbw
 * @date 2025-08-20
 */
@Tag(name = "管理后台 - 三方商品配方管理")
@Slf4j
@RestController
@RequestMapping("/commodity/triInventory")
public class TriRecipeController {

    @Resource
    private ITriRecipeService triRecipeService;

    @Operation(summary = "查询三方商品表")
    @PostMapping("/getTriFormulation")
    public CommonResult<PageResult<TriFormulationDO>> getTriFormulation(@Valid @RequestBody TriFormulationReqVO recipeReqVO) {
        PageResult<TriFormulationDO> list =triRecipeService.selectTriFormulationList(recipeReqVO);
        return success(list);
    }

    @Operation(summary = "查询关联三方商品表")
    @PostMapping("/getTriFormulationRelated")
    public CommonResult<PageResult<TriFormulationRelatedRespVO>> getTriFormulationRelated(@Valid @RequestBody TriFormulationReqVO recipeReqVO) {
        PageResult<TriFormulationRelatedRespVO> list =triRecipeService.selectTriFormulationRelatedList(recipeReqVO);
        return success(list);
    }


    @Operation(summary = "新增三方商品表")
    @PostMapping("/insertTriFormulation")
    public CommonResult<TriFormulationDO> insertTriFormulation(@Valid @RequestBody TriRecipeInsertOrUpdateReqVO triRecipeReqVO) {
        TriFormulationDO triFormulationDO = triRecipeService.insertTriFormulation(triRecipeReqVO);
        return success(triFormulationDO);
    }

    @Operation(summary = "删除三方商品表与配方和关联原料关系")
    @GetMapping ("/delTriFormulation")
    public CommonResult<Boolean> delTriFormulation(@Valid @RequestParam("triId") Long triId) {
        triRecipeService.delTriFormulation(triId);
        return success(true);
    }

    @Operation(summary = "根据三方商品配方id查询商品配方列表")
    @GetMapping("/getTriRecipe")
    public CommonResult<List<TriRecipeDO>> getTriRecipe(@RequestParam("triId") Long id) {
        List<TriRecipeDO> list =triRecipeService.selectById(id);
        return success(list);
    }

    @Operation(summary = "根据三方渠道与商品名称查询商品配方列表")
    @PostMapping("/getTriRecipeByNames")
    public CommonResult<List<TriRecipeDO>> getTriRecipe(@Valid @RequestBody TriRecipeReqVO recipeReqVO) {
        List<TriRecipeDO> list =triRecipeService.selectByNames(recipeReqVO);
        return success(list);
    }

    @Operation(summary = "选择原料新增商品规格配方")
    @PostMapping("/insertTriRecipeMaterials")
    public CommonResult<Boolean> insertTriRecipeMaterial(@RequestBody @Valid List<TriRecipeMaterialSelectReqVO> selectReqVOS) {
        triRecipeService.batchInsert(selectReqVOS);
        return success(true);
    }

    @Operation(summary = "配方保存与修改")
    @PostMapping("/insertTriRecipe")
    public CommonResult<Boolean> insertTriRecipe(@RequestBody @Valid List<TriFormulationRecipeReqVO> list) throws InterruptedException {

        if (CollectionUtil.isNotEmpty(list)){
            Long triId = list.get(0).getTriId();
            triRecipeService.insertRecipe(list,triId);
        }
        return success(true);
    }

    @Operation(summary = "删除配方时清空取消选中替换商品列表")
    @GetMapping("/deleteTriRecipe")
    public CommonResult<Boolean> deleteTriRecipe(
                                              @RequestParam("triId") @Valid @NotNull Long triId,
                                              @RequestParam("materialsId") @Valid @NotNull Long materialsId) {
        triRecipeService.truncRecipeMaterial(triId, materialsId);
        return success(true);
    }

    @Operation(summary = "查询配方中原料下所有已取消替换原料")
    @GetMapping("/getTriUnSelectedRecipeMaterials")
    public CommonResult<List<TriRecipeMaterialUnSelectedRespVO>> getTriUnSelectedRecipeMaterial(@RequestParam("triId") @Valid @NotNull Long triId,
                                                                                             @RequestParam("replaceMaterialsId") @Valid @NotNull Long replaceMaterialsId) {
        List<TriRecipeMaterialUnSelectedRespVO> result = triRecipeService.getUnSelectedRecipeMaterials(triId, replaceMaterialsId);
        return success(result);
    }

    @Operation(summary = "保存配方中原料下所有已取消替换原料")
    @PostMapping("/saveTriUnSelectedRecipeMaterials")
    public CommonResult<Boolean> saveTriUnSelectedRecipeMaterials(@RequestBody @Valid List<TriRecipeMaterialUnSelectedReqVO> recipeMaterialUnSelectedReqVOList) {

        triRecipeService.saveUnSelectedRecipeMaterial(recipeMaterialUnSelectedReqVOList);
        return success(true);
    }
}
