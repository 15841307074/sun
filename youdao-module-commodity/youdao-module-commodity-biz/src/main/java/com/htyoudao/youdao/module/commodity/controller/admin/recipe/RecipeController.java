package com.htyoudao.youdao.module.commodity.controller.admin.recipe;

import cn.hutool.core.collection.CollectionUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.*;
import com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO.TriFormulationRelatedRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO.TriFormulationReqVO;
import com.htyoudao.youdao.module.commodity.service.inventory.ICommodityRecipeService;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.RECIPE_COMMODITY_INSERT_ASYNC_ERROR;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.RECIPE_COMMODITY_INSERT_ERROR;


/**
 * 商品配方Controller
 *
 * @author lbw
 * @date 2025-08-20
 */
@Tag(name = "管理后台 - 商品配方管理")
@Slf4j
@RestController
@RequestMapping("/commodity/inventory")
public class RecipeController {

    @Resource
    private ICommodityRecipeService commodityRecipeService;

    @Resource
    private ICommoditySpusService commoditySpusService;

    @Resource
    private RedissonClient redissonClient;

    @Operation(summary = "根据商品id查询商品规格配方")
    @GetMapping("/getRecipeByCommodityId/{commodityId}")
    public CommonResult<List<RecipeCommoditySkuRespVO>> getCommodityRecipe(@PathVariable("commodityId") Long commodityId) {
        List<RecipeCommoditySkuRespVO> list =commodityRecipeService.selectByCommodityId(commodityId);
        return success(list);
    }

    @Operation(summary = "根据商品id查询单品商品列表")
    @PostMapping("/getCommodityList")
    public CommonResult<PageResult<RecipeCommodityRespVO>> getCommodityList(@RequestBody @Valid RecipeCommodityReqVO recipeCommodityReqVO) {
        PageResult<RecipeCommodityRespVO> list = commoditySpusService.selectRecipeCommodity(recipeCommodityReqVO);
        return success(list);
    }

    @Operation(summary = "根据商品id查询所有归属门店")
    @PostMapping("/getCommodityStoreList")
    public CommonResult<PageResult<StoreInfoDTO>> getCommodityStoreList(@RequestBody RecipeCommodityStoreReqVO recipeCommodityStoreReqVO) {
        PageResult<StoreInfoDTO> list = commoditySpusService.getCommodityStoreList(recipeCommodityStoreReqVO);
        return success(list);
    }


    @Operation(summary = "根据商品名称查询已配置配方商品列表")
    @PostMapping("/getCommodityRecipeList")
    public CommonResult<PageResult<RecipeCommoditySkuListRespVO>> getCommodityRecipeList(@RequestBody RecipeCommodityReqVO recipeCommodityReqVO) {
        PageResult<RecipeCommoditySkuListRespVO> list = commoditySpusService.selectCommodityRecipeList(recipeCommodityReqVO);
        return success(list);
    }

    @Operation(summary = "删除配方时清空取消选中替换商品列表")
    @DeleteMapping("/deleteRecipe/{commodityId}/{skuId}/{materialsId}")
    public CommonResult<Boolean> deleteRecipe(@PathVariable("commodityId") @Valid @NotNull Long commodityId,
                                              @PathVariable("skuId") @Valid @NotNull Long skuId,
                                              @PathVariable("materialsId") @Valid @NotNull Long materialsId) {
        commodityRecipeService.truncRecipeMaterial(commodityId, skuId, materialsId);
        return success(true);
    }

    @Operation(summary = "选择原料新增商品规格配方")
    @PostMapping("/insertRecipeMaterials")
    public CommonResult<Boolean> insertRecipeMaterial(@RequestBody @Valid List<RecipeMaterialSelectReqVO> recipeMaterialSelectReqVOList) {
        commodityRecipeService.batchInsert(recipeMaterialSelectReqVOList);
        return success(true);
    }

    @Operation(summary = "配方保存与修改")
    @PostMapping("/insertRecipe")
    public CommonResult<Boolean> insertRecipe(@RequestBody @Valid List<RecipeReqVO> recipeReqVOList) throws InterruptedException {

        if (CollectionUtil.isNotEmpty(recipeReqVOList)){
            Long commodityId = recipeReqVOList.get(0).getCommodityId();
            RLock lock = redissonClient.getLock("insertRecipe_" + commodityId);
            try {
                boolean tryLock = lock.tryLock(3, TimeUnit.SECONDS);
                if (!tryLock){
                    log.info("配方保存与修改，获取锁失败," + commodityId);
                    throw exception(RECIPE_COMMODITY_INSERT_ASYNC_ERROR);
                }
                commodityRecipeService.insertRecipe(recipeReqVOList,commodityId);
                return success(true);
            }finally {
                if (lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        }else {
            return error(RECIPE_COMMODITY_INSERT_ERROR);
        }
    }

    // todo调整参数为recipeId
    @Operation(summary = "查询配方中原料下所有已取消替换原料")
    @GetMapping("/getUnSelectedRecipeMaterials/{commodityId}/{skuId}/{replaceMaterialsId}")
    public CommonResult<List<RecipeMaterialUnSelectedRespVO>> getUnSelectedRecipeMaterial(@PathVariable("commodityId") @Valid @NotNull Long commodityId,
                                                                                          @PathVariable("skuId") @Valid @NotNull Long skuId,
                                                                                          @PathVariable("replaceMaterialsId") @Valid @NotNull Long replaceMaterialsId) {
        List<RecipeMaterialUnSelectedRespVO> result = commodityRecipeService.getUnSelectedRecipeMaterials(commodityId, skuId, replaceMaterialsId);
        return success(result);
    }

    @Operation(summary = "保存配方中原料下所有已取消替换原料")
    @PostMapping("/saveUnSelectedRecipeMaterials")
    public CommonResult<Boolean> saveUnSelectedRecipeMaterials(@RequestBody @Valid List<RecipeMaterialUnSelectedReqVO> recipeMaterialUnSelectedReqVOList) {

        commodityRecipeService.saveUnSelectedRecipeMaterial(recipeMaterialUnSelectedReqVOList);
        return success(true);
    }
}
