package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.TemplatePriceReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.TemplatePriceUpdatePriceVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchDown.BatchDownSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp.SingleUpRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.*;

import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpConfirmReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchDown.StoreBatchDownSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchDown.StoreBatchDownSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchUp.StoreBatchUpSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchUp.StoreBatchUpSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle.StoreSingleSimpleReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle.StoreSingleSimpleRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateSortSpuReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.*;
import com.htyoudao.youdao.module.commodity.service.job.JobService;
import com.htyoudao.youdao.module.commodity.service.storeSpu.ICommodityStoreSpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


/**
 * <p>
 * 门店下商品表 前端控制器
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Tag(name = "门店下商品表")
@Slf4j
@RestController
@RequestMapping("/commodity/commodity-store-spu")
public class CommodityStoreSpuController {


    @Resource
    private ICommodityStoreSpuService commodityStoreSpuService;

    @Operation(summary = "删除模版")
    @PreAuthorize("@ss.hasPermission('commodity:temp:delete')")
    @DeleteMapping("/template/deleteTemplate/{commodityTemplateId}")
    public CommonResult<Boolean> removeTemplateNew(@PathVariable Long commodityTemplateId) {
        commodityStoreSpuService.deleteCommodityTemplateByCommodityTemplateIdNew(commodityTemplateId);
        return success(true);
    }


    @Operation(summary = "查询模板商品列表")
    @GetMapping("/template/listCommodityTemplate")
    @PreAuthorize("@ss.hasPermission('commodity:temp:query')")
    public CommonResult<List<TemplateSpusRespVO>> listCommodityTemplate(@RequestParam(value = "templateId")  Long templateId, @RequestParam(value = "templateCategoryId")  Long templateCategoryId) {
        List<TemplateSpusRespVO> commodityTemplates = commodityStoreSpuService.selectCommodityTemplateListNew(templateId, templateCategoryId);
        return success(commodityTemplates);
    }


    @Operation(summary = "删除模板分类")
    @GetMapping("/template/deleteCategory")
    @PreAuthorize("@ss.hasPermission('commodity:temp:deleteCate')")
    public CommonResult<Boolean> deleteCategory(@RequestParam(value = "templateCategoryId")  Long templateCategoryId) {

        commodityStoreSpuService.deleteCommodityCategoryByIds(templateCategoryId);
        return success(true);
    }


    //@PreAuthorize("@ss.hasPermi('commodity:category:remove')")
    @Operation(summary = "删除模板商品")
    @PostMapping("/template/deleteCommodityTemplate")
    @PreAuthorize("@ss.hasPermission('commodity:temp:deleteSpu')")
    public CommonResult<Boolean> deleteCommodityTemplate(@Valid @RequestBody TemplateDelComReqVO comReqVO) {
        commodityStoreSpuService.deleteCommodityTemplatePriceByCommodityIds(comReqVO);
        return success(true);
    }

    @Operation(summary = "修改商品模板价格")
    @PostMapping("/template/updateTemplatePrice")
    @PreAuthorize("@ss.hasPermission('commodity:temp:update')")
    public CommonResult<Boolean> updateTemplatePriceV3(@Valid @RequestBody TemplatePriceUpdatePriceVO templatePriceVO) {
        commodityStoreSpuService.updateTemplatePriceV3(templatePriceVO);
        return success(true);
    }





    @Operation(summary = "删除门店下所有商品")
    @GetMapping("/deleteAllSpuByStoreId")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@ss.hasPermission('commodity:store:deleteAllSpu')")
    public CommonResult<Boolean> deleteAllCommoditysByStoreIdV3(@RequestParam(value = "storeId")  Long storeId) {
        commodityStoreSpuService.deleteSpuByStoreId(storeId);
        return success(true);
    }

    @Operation(summary = "删除所有门店的所有商品")
    @GetMapping("/deleteAllStoreSpu")
    @PreAuthorize("@ss.hasPermission('commodity:store:deleteStoreSpu')")
    public CommonResult<Boolean> deleteAllStoreSpu() {
        commodityStoreSpuService.deleteAllStoreSpu();
        return success(true);
    }






    @Operation(summary = "删除门店分类")
    @GetMapping("/deleteCategory")
    @PreAuthorize("@ss.hasPermission('commodity:store:deleteCate')")
    public CommonResult<Boolean> deleteStoreCategory(@RequestParam Long commodityStoreCategoryId) {
        commodityStoreSpuService.deleteStoreCategory(commodityStoreCategoryId);
        return success(true);
    }


    @Operation(summary = "门店分类修改")
    @PostMapping("/v3/updateCategory")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    public CommonResult<Boolean> updateCategory(@Valid @RequestBody StoreCategoryUpdateReqVO updateReqVO) {
        commodityStoreSpuService.updateStoreCategory(updateReqVO);
        return success(true);
    }


    @Operation(summary = "门店下批量删除商品")
    @PostMapping("/deleteStoreSpus")
    @PreAuthorize("@ss.hasPermission('commodity:store:deleteSelect')")
    public CommonResult<Boolean> deleteStoreSpus(@RequestBody List<Long> storeSpuIds) {
        commodityStoreSpuService.deleteStoreSpus(storeSpuIds);
        return success(true);
    }



    @Operation(summary = "门店单品下架前查询逻辑")
    @PostMapping("/V3/storeSingleDownSelect")
    @PreAuthorize("@ss.hasPermission('commodity:store:query')")
    public CommonResult<List<CommodityStoreSpuRespVo>> storeSingleDownSelectV3(@Valid @RequestBody StoreItemDelistReqVo storeItemDelistReqVo) {
        List<CommodityStoreSpuRespVo> storeSpu =  commodityStoreSpuService.storeSingleDownSelectV3(storeItemDelistReqVo);
        return success(storeSpu);
    }


    @Operation(summary = "门店单品下架")
    @PostMapping("/V3/singleDown")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    public CommonResult<Boolean> singleDownV3(@Valid @RequestBody StoreItemDelistReqVo delistReqVo) {
        delistReqVo.setIsApp(Boolean.FALSE);
        commodityStoreSpuService.singleDownV3(delistReqVo);
        return success(true);
    }


    /*@Operation(summary = "门店单品上架")
    @PostMapping("/V3/singleUp")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    public CommonResult<Boolean> singleUpV3(@Valid @RequestBody StoreItemListingReqVo itemListingReqVo) {
        itemListingReqVo.setIsApp(Boolean.FALSE);
        commodityStoreSpuService.singleUpV3(itemListingReqVo);
        return success(true);
    }*/

    @Operation(summary = "门店单品联动套餐上架查询")
    @PostMapping("/singleUpSelect")
    @PreAuthorize("@ss.hasPermission('commodity:store:query')")
    public CommonResult<StoreSingleUpRespVO> singleUpSelect(@Valid @RequestBody StoreSingleUpReqVO singleUpReqVo) {
        singleUpReqVo.setIsApp(Boolean.FALSE);
        StoreSingleUpRespVO storeSingleUpRespVO =  commodityStoreSpuService.singleUpSelect(singleUpReqVo);
        return success(storeSingleUpRespVO);
    }


    @Operation(summary = "门店单品联动套餐上架确认")
    @PostMapping("/singleUpConfirm")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    public CommonResult<Boolean> singleUpConfirm(@Valid @RequestBody StoreSingleUpConfirmReqVO singleUpConfirmReqVO) {
        singleUpConfirmReqVO.setIsApp(Boolean.FALSE);
         commodityStoreSpuService.singleUpConfirm(singleUpConfirmReqVO);
        return success(true);
    }

    @Operation(summary = "门店批量下架查询")
    @PreAuthorize("@ss.hasPermission('commodity:store:query')")
    @PostMapping("/batchDownSelect")
    public CommonResult<StoreBatchDownSelectRespVO> batchDownSelect(@Valid @RequestBody StoreBatchDownSelectReqVO storeBatchDownSelectReqVO) {
        StoreBatchDownSelectRespVO batchDownSelect  =  commodityStoreSpuService.batchDownSelect(storeBatchDownSelectReqVO);
        return success(batchDownSelect);
    }

    @Operation(summary = "门店批量下架确认")
    @PostMapping("/batchDownConfirm")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    public CommonResult<Boolean> batchDownConfirm(@Valid @RequestBody StoreBatchDownSelectReqVO storeBatchDownSelectReqVO) {
        storeBatchDownSelectReqVO.setApp(Boolean.FALSE);
        commodityStoreSpuService.batchDownConfirm(storeBatchDownSelectReqVO);
        return success(true);
    }

    @Operation(summary = "门店批量上架查询")
    @PreAuthorize("@ss.hasPermission('commodity:store:query')")
    @PostMapping("/batchUpSelect")
    public CommonResult<StoreBatchUpSelectRespVO> batchUpSelect(@Valid @RequestBody StoreBatchUpSelectReqVO storeBatchUpSelectReqVO) {
        StoreBatchUpSelectRespVO storeBatchUpSelectRespVO = commodityStoreSpuService.batchUpSelect(storeBatchUpSelectReqVO);
        return success(storeBatchUpSelectRespVO);
    }

    @Operation(summary = "门店批量上架确认")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    @PostMapping("/batchUpConfirm")
    public CommonResult<Boolean> batchUpConfirm(@Valid @RequestBody StoreBatchUpSelectReqVO storeBatchUpSelectReqVO){

        storeBatchUpSelectReqVO.setApp(Boolean.FALSE);
        commodityStoreSpuService.batchUpConfirm(storeBatchUpSelectReqVO);
        return success(true);
    }



    @Operation(summary = "门店套餐上架查询")
    @PostMapping("/V3/packageSelect")
    @PreAuthorize("@ss.hasPermission('commodity:store:query')")
    public CommonResult<List<CommodityStoreSingleRespVO>> packageSelectV3(@Valid @RequestBody StoreItemListingReqVo listingReqVo) {

        List<CommodityStoreSingleRespVO> singleList = commodityStoreSpuService.packageSelectV3(listingReqVo);
        return success(singleList);
    }


    @Operation(summary = "门店套餐上下架")
    @PostMapping("/V3/packageUpAndDown")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    public CommonResult<Boolean> packageUpAndDownV3(@Valid @RequestBody StoreItemUnmountReqVo itemUnmountReqVo) {
        itemUnmountReqVo.setIsApp(Boolean.FALSE);
        commodityStoreSpuService.packageUpAndDownV3(itemUnmountReqVo);

        return success(true);
    }



    @Operation(summary = "门店商品详情")
    @PostMapping("/v3/getSpusInfo")
    @PreAuthorize("@ss.hasPermission('commodity:store:query')")
    public CommonResult<CommodityStoreRespVo> getSpusInfoV3(@Valid @RequestBody CommodityStoreSpuDetailReqVO detailReqVO) {

        CommodityStoreRespVo commodityStoreRespVo = commodityStoreSpuService.getSpusInfoV3(detailReqVO);
        return success(commodityStoreRespVo);
    }

    @Operation(summary = "更新商品")
    @PostMapping("/v3/updateCommodity")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    public CommonResult<Boolean> updateCommodityV3(@Valid @RequestBody CommodityStoreUpdateReqVo updateReqVo) {
        commodityStoreSpuService.updateCommodityPremiseV3(updateReqVo);
        return success(true);
    }


    @Operation(summary = "门店商品查询列表")
    @PostMapping("/v3/getAllSpu")
    @PreAuthorize("@ss.hasPermission('commodity:store:query')")
    public CommonResult<List<CommodityStoreRespVo>> getAllSpuV3(@Valid @RequestBody CommodityStoreAllReqVO allReqVO) {

        List<CommodityStoreRespVo> commodityStoreSpuList = commodityStoreSpuService.getAllSpuV3(allReqVO);

        return success(commodityStoreSpuList);
    }

    @Operation(summary = "门店商品拖动排序")
    @PostMapping("/sort/spu")
    @PreAuthorize("@ss.hasPermission('commodity:store:update')")
    public CommonResult<Boolean> sortSpu(@Valid @RequestBody List<CommodityStoreSortSpuReqVO> sortReqVOS) {
        commodityStoreSpuService.sortSpu(sortReqVOS);
        return success(true);
    }


    @Operation(summary = "损耗记录-门店商品查询列表")
    @PostMapping("/v3/getByStoreSpuList")
    public CommonResult<List<CommodityStoreRespVo>> getByStoreSpuList(@Valid @RequestBody CommodityStoreAllReqVO allReqVO) {

        List<CommodityStoreRespVo> commodityStoreSpuList = commodityStoreSpuService.getByStoreSpuList(allReqVO);

        return success(commodityStoreSpuList);
    }

    @Operation(summary = "通过门点 ID 和名称返回商品数据")
    @PostMapping("/getStoreSingle")
    public CommonResult<List<StoreSingleSimpleRespVO>> getStoreSingle(@Valid @RequestBody StoreSingleSimpleReqVO storeSingleSimpleReqVO){

        List<StoreSingleSimpleRespVO> storeSingleSimpleRespVOS = commodityStoreSpuService.getStoreSingle(storeSingleSimpleReqVO);
        return success(storeSingleSimpleRespVOS);
    }

}
