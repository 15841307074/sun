package com.htyoudao.youdao.module.commodity.controller.admin.spus;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.*;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchDown.BatchDownSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchDown.BatchDownSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchUp.BatchUpSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchUp.BatchUpSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category.CategoryDelReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category.CategoryUpdateReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category.CommodityCateDateRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.shelfLock.CommoditySpuLockReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single.CommodityGroupSingleRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single.CommoditySingleDownReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single.CommoditySingleUpReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp.SingleUpConfirmReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp.SingleUpRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.BASE_SPU_SPU_ID_NOT_PASSED;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.COMMODITY_NO_PRODUCT_SELECTED;


/**
 * 商品Controller
 *
 * @author Qizhongann
 * @date 2024-01-16
 */
@Tag(name = "管理后台 - 商品")
@RestController
@RequestMapping("/commodity/spus")
public class CommoditySpusController {
    @Resource
    private ICommoditySpusService commoditySpusService;


    @Operation(summary = "删除商品分类")
    @PostMapping("/V3/deleteCategory")
    @PreAuthorize("@ss.hasPermission('commodity:spu:delete')")
    public CommonResult<Boolean> deleteCategoryV3(@Valid @RequestBody CategoryDelReqVo delReqVo) {
        commoditySpusService.deleteCategoryV3(delReqVo);
        return success(true);
    }


    @Operation(summary = "新建商品")
    @PostMapping("/V3/createSpu")
    @PreAuthorize("@ss.hasPermission('commodity:spu:create')")
    public CommonResult<Boolean> createSpuV3(@Valid @RequestBody CommoditySpusSaveReqVo saveReqVo) {
        commoditySpusService.createSpuV3(saveReqVo);
        return success(true);
    }



    @Operation(summary = "修改商品")
    @PostMapping("/V3/updateSpu")
    @PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<Boolean>  updateSpuV3(@Valid @RequestBody CommoditySpusUpdateReqVo updateReqVo) {
        commoditySpusService.updateSpuV3(updateReqVo);
        return success(true) ;
    }

    @Operation(summary = "过滤隐藏商品ID集合")
    @GetMapping("/V3/filterHiddenCommodityIds")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<CommodityHiddenFilterRespVO> filterHiddenCommodityIds() {
        return success(commoditySpusService.filterHiddenCommodityIds());
    }


    @Operation(summary = "商品分组列表接口")
    @PostMapping("/V3/getCategoryList")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<List<CategoryRespVo>> getCategoryListV3(@Valid @RequestBody CommodityTypeReqVo typeReqVo){
        List<CategoryRespVo> list = commoditySpusService.getCategoryListV3(typeReqVo);
        return success(list);
    }

    @Operation(summary = "按照分类查询商品")
    @PostMapping("/V3/getSpuList")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<List<CommodityCateDateRespVo>> getSpuListV3(@Valid @RequestBody CommoditySpusQueryReqVo queryReqVo) {
        List<CommodityCateDateRespVo> spusList =  commoditySpusService.getSpuListV3(queryReqVo);
        return success(spusList);
    }
    @Operation(summary = "根据商品id查询商品")
    @GetMapping("/V3/getSpuById")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<CommoditySpusByIdRespVo> getSpuByIdV3(@RequestParam(value = "commodityId", required = false) Long id) {
        CommoditySpusByIdRespVo byIdRespVo = commoditySpusService.getSpuByIdV3(id);
        return success(byIdRespVo);
    }


    @Operation(summary = "删除商品")
    @GetMapping("/V3/deleteSpuById")
    @PreAuthorize("@ss.hasPermission('commodity:spu:delete')")
    public CommonResult<Boolean> deleteSpuByIdV3(@RequestParam(value = "commodityId", required = false) Long id) {
        commoditySpusService.deleteSpuByIdV3(id);
        return success(true);
    }

    /**
     * 暂时不用，代码保留
     * @return
     */

    /*@Operation(summary = "新增模版")
    @PostMapping("/v3/addTemplate")
    public CommonResult<Boolean> addTemplate(@Valid @RequestBody TemplatePriceReqVO templatePriceVO){
        List<Long> commodityIds = templatePriceVO.getCommodityIds();
        Long commodityTemplateId = templatePriceVO.getCommodityTemplateId();

        commoditySpusService.addCommodityTemplateForCommodityIds(commodityIds,commodityTemplateId);
        return success(true);
    }*/


    @Operation(summary = "模糊搜索商品")
    @PostMapping("/V3/fuzzySearchCommodity")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<List<CategoryRespVo>> fuzzySearchCommodityV3(@RequestBody CommoditySpusSearchReqVo searchReqVo) {
        List<CategoryRespVo> categoryList = commoditySpusService.fuzzySearchCommodityV3(searchReqVo);
    return success(categoryList);
}

    @Operation(summary = "批量修改")
    @PostMapping("/V3/batchModification")
    @PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult batchModificationV3(@Valid @RequestBody CommodityBatchEditReqVo batchEditReqVo) {

        List<CommoditySpus> spusList =  commoditySpusService.batchModificationV3(batchEditReqVo);
        if (ObjectUtil.isNotEmpty(spusList)){
            return success(spusList);
        }
       return success(true );

    }

    @Operation(summary = "商品总数量查询")
    @GetMapping("/V3/getSpuCount")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<List<CommodityCountRespVO>> getSpuCountV3() {
        List<CommodityCountRespVO> countVOList = commoditySpusService.getSpuCountV3();
        return success(countVOList);
    }

    @Operation(summary = "批量排序")
    @PostMapping("/V3/sortSpu")
    @PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<Boolean> sortSpuV3(@Valid @RequestBody List<CommoditySpusSortReqVo> spusSortReqVos) {
        commoditySpusService.sortSpuV3(spusSortReqVos);
        return success(true);
    }

    @Operation(summary = "更新商品名称")
    @PostMapping("/V3/UpdateName")
    @PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<Boolean> updateNameV3(@Valid @RequestBody CommodityUNameReqVo uNameReqVo) {
        commoditySpusService.updateNameV3(uNameReqVo);
        return success(true);
    }


    @Operation(summary = "修改分类")
    @PostMapping("/V3/updateCategory")
    @PreAuthorize("@ss.hasPermission('commodity:cate:update')")
    public CommonResult<Boolean> updateCategoryV3(@Valid @RequestBody CategoryUpdateReqVo updateReqVo){
        commoditySpusService.updateCategoryV3(updateReqVo);
        return success(true);
    }



    @Operation(summary = "搜索单规格无小料无属性的接口")
    @GetMapping("/V3/listForCommodity")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<List<CommoditySpusByIdRespVo>> listForCommodityV3(@RequestParam(required = false) Long commodityId) {
        List<CommoditySpusByIdRespVo> spusList = commoditySpusService.listForCommodityV3(commodityId);
        return success(spusList);

    }


    @Operation(summary = "搜索已上下架商品 1下架 2上架")
    @GetMapping("/V3/SelectDownSpu/{isDown}")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<List<CategoryRespVo>> selectDownSpuV3(@RequestParam Integer isDown) {
        List<CategoryRespVo> categoryList = commoditySpusService.selectDownSpuV3(isDown);
        return success(categoryList);
    }

    /**
     * 保留
     * @param singleDownReqVo
     * @return
     */
/*    @Operation(summary = "定时任务 给商品做上下架")
    @GetMapping("/V3/xxjobSpuUpOrDown")
    public CommonResult<Boolean> xxjobSpuUpOrDownV3() {
        commoditySpusService.xxjobSpuUpOrDownV3();

        return success(true);
    }*/




    @Operation(summary = "连锁商品库单品下架前查询逻辑")
    @PostMapping("/V3/singleDownSelect")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<List<CommoditySpusByIdRespVo>> getOffShelfProductsV3(@Valid @RequestBody CommoditySingleDownReqVo singleDownReqVo) {
        if (ObjectUtil.isEmpty(singleDownReqVo.getCommodityId()) || ObjectUtil.isEmpty(singleDownReqVo.getChooseView())){
            throw exception(COMMODITY_NO_PRODUCT_SELECTED);
        }

        List<CommoditySpusByIdRespVo> spusList = commoditySpusService.getOffShelfProductsV3(singleDownReqVo);

        return success(spusList);
    }

    @Operation(summary = "连锁商品库单品下架")
    @PostMapping("/V3/singleDown")
    @PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<Boolean> singleDownV3(@Valid @RequestBody CommoditySingleDownReqVo singleDownReqVo) {
        if (ObjectUtil.isEmpty(singleDownReqVo.getCommodityId())){
            throw exception(BASE_SPU_SPU_ID_NOT_PASSED);
        }

        commoditySpusService.singleDownV3(singleDownReqVo);
        return success(true);
    }

   /* @Operation(summary = "连锁商品库单品上架")
    @PostMapping("/V3/singleUp")
    @PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<Boolean> singleUpV3(@Valid @RequestBody CommoditySingleUpReqVo singleUpReqVo) {
        if (ObjectUtil.isEmpty(singleUpReqVo.getCommodityId())){
            throw exception(BASE_SPU_SPU_ID_NOT_PASSED);
        }
        commoditySpusService.singleUpV3(singleUpReqVo);
        return success(true);
    }*/

    @Operation(summary = "连锁商品库单品联动套餐上架查询")
    @PostMapping("/singleUpSelect")
    //@PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<SingleUpRespVO> singleUpSelect(@Valid @RequestBody CommoditySingleUpReqVo singleUpReqVo) {
        SingleUpRespVO singleUpRespVO =  commoditySpusService.singleUpSelect(singleUpReqVo);
        return success(singleUpRespVO);
    }

    @Operation(summary = "连锁商品库单品联动套餐上架操作")
    @PostMapping("/singleUpConfirm")
    @PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<Boolean> singleUpConfirm(@Valid @RequestBody SingleUpConfirmReqVO singleUpReqVo) {
          commoditySpusService.singleUpConfirm(singleUpReqVo);
        return success(true);
    }

    /**
     * 1.区分批量下架的单品与套餐 套餐直接下架 单品直接下架 zipin
     *  2.拿取批量里的单品 id 反查套餐 商品 分组 子品 （一次查询）
     *  3.循环每个套餐
     *  4.查看哪些套餐构成下架条件
     *  5.将查出来的套餐去除传进来的套餐
     *  6.去除后的套餐信息返回给前端弹窗确认（下架的单品们，下架的套餐们，提示还有需要下架的套餐们）
     *  7.得到前端确认，将需要下架的商品和子品做下架处理
     */
    @Operation(summary = "连锁商品库批量下架查询")
    @PostMapping("/batchDownSelect")
    //@PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<BatchDownSelectRespVO> batchDownSelect(@Valid @RequestBody BatchDownSelectReqVO batchDownSelectReqVO) {
        BatchDownSelectRespVO batchDownSelect  =  commoditySpusService.batchDownSelect(batchDownSelectReqVO);
        return success(batchDownSelect);
    }

    @Operation(summary = "连锁商品库批量下架确认")
    @PostMapping("/batchDownConfirm")
    //@PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<Boolean> batchDownConfirm(@Valid @RequestBody BatchDownSelectReqVO batchDownSelectReqVO) {
          commoditySpusService.batchDownConfirm(batchDownSelectReqVO);
        return success(true);
    }




    @Operation(summary = "连锁商品库批量上架查询")
    @PostMapping("/batchUpSelect")
    public CommonResult<BatchUpSelectRespVO> batchUpSelect(@Valid @RequestBody BatchUpSelectReqVO batchUpSelectReqVO) {
        BatchUpSelectRespVO batchUpSelectRespVO = commoditySpusService.batchUpSelect(batchUpSelectReqVO);
        return success(batchUpSelectRespVO);

    }

    @Operation(summary = "连锁商品库批量上架确认")
    @PostMapping("/batchUpConfirm")
    public CommonResult<Boolean> batchUpConfirm(@Valid @RequestBody BatchUpSelectReqVO batchUpSelectReqVO) {

        commoditySpusService.batchUpConfirm(batchUpSelectReqVO);

        return success(true);
    }





    /**
     * 套餐上架查询
     */
    @Operation(summary = "连锁商品库套餐上架查询")
    @PostMapping("/V3/packageSelect")
    @PreAuthorize("@ss.hasPermission('commodity:spu:query')")
    public CommonResult<List<CommodityGroupSingleRespVo>> packageSelectV3(@Valid @RequestBody SetMealSingleUpReqVo singleUpReqVo) {
        if (ObjectUtil.isEmpty(singleUpReqVo.getCommodityId())){
            throw exception(BASE_SPU_SPU_ID_NOT_PASSED);
        }
        List<CommodityGroupSingleRespVo> singleList = commoditySpusService.packageSelectV3(singleUpReqVo);
        return success(singleList);
    }

    /**
     * 套餐上下架
     */
    @Operation(summary = "连锁商品库套餐上下架")
    @PostMapping("/V3/packageUpAndDown")
    @PreAuthorize("@ss.hasPermission('commodity:spu:update')")
    public CommonResult<Boolean> packageUpAndDownV3(@Valid @RequestBody SetMealUpAndDownReqVo upAndDownReqVo) {
        if (ObjectUtil.isEmpty(upAndDownReqVo.getCommodityId())){
            throw exception(BASE_SPU_SPU_ID_NOT_PASSED);
        }
        commoditySpusService.packageUpAndDownV3(upAndDownReqVo);
        return success(true);
    }

    /** 不用 保留
     * 商品新加字段同步单品状态信息
     */
    @PostMapping("/V3/checkStatus")
    public CommonResult<Boolean> checkStatusV3() {
        commoditySpusService.checkStatusV3();
        return success(true);
    }



    @Operation(summary = "优惠券商品选择器")
    @GetMapping("/coupon/spu/list")
    public CommonResult<PageResult<CommodityCouponSpuVO>> couponSpuList(CommodityCouponSpuReqVO queryReqVo) {
        return success(commoditySpusService.couponSpuList(queryReqVo));
    }


    @Operation(summary = "修改商品上架锁")
    @PostMapping("/updateShelfLock")
    @PreAuthorize("@ss.hasPermission('commodity:spu:updateShelfLock')")
    public CommonResult<Boolean> updateShelfLock(@Valid @RequestBody CommoditySpuLockReqVO reqVO) {
        commoditySpusService.updateShelfLock(reqVO);
        return success(true);
    }
}
