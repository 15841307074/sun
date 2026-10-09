package com.htyoudao.youdao.module.member.controller.admin.pointsProduct;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.*;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;
import com.htyoudao.youdao.module.member.service.pointsProduct.IPointsProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


/**
 * 积分商品Controller
 *
 * @author Qizhongnan
 * @date 2024-02-02
 */
@Tag(name = "管理后台 - 积分商品")
@RestController
@RequestMapping("/member/points-product")
@Validated
public class PointsProductController {
    @Autowired
    private IPointsProductService pointsProductService;

    @Operation(summary = "查询积分商品列表")
    @GetMapping("/list")
    public CommonResult<PageResult<PointsProductDO>> list(@RequestParam(value = "pageNo", required = false) long pageNo,
                                                          @RequestParam(value = "pageSize", required = false) long pageSize,
                                                          @Valid PointsProductPageListReqVo pageListReqVo) {
        return success(pointsProductService.selectPointsProductListPage(pageNo, pageSize, pageListReqVo));
    }


    @Operation(summary = "新增积分商品")
    @PostMapping
    public CommonResult<Integer> add(@Valid @RequestBody PointsProductSaveReqVo saveReqVo) {
        return CommonResult.success(pointsProductService.insertPointsProduct(saveReqVo));
    }


    @Operation(summary = "修改积分商品")
    @PostMapping("/edit")
    public CommonResult<Integer> edit(@Valid @RequestBody PointsProductEditReqVo editReqVo) {
        return CommonResult.success(pointsProductService.updatePointsProduct(editReqVo));
    }

    @Operation(summary = "修改积分商品上下架状态")
    @PutMapping("/update-availability")
    public CommonResult<Integer> updateAvailability(@Valid @RequestBody PointsProductAvailabilityReqVO reqVO) {
        return CommonResult.success(pointsProductService.updateAvailability(reqVO));
    }


    /**
     * 获取 PC 管理后台积分商品详情，并补充附件类型和优惠券实时信息。
     */
    @Operation(summary = "获取积分商品详细信息")
    @GetMapping(value = "/{productId}")
    public CommonResult<PointsProductDO> getInfo(@PathVariable("productId") Long productId) {
        return CommonResult.success(pointsProductService.selectAdminPointsProductByProductId(productId));
    }



    /**
     * 小程序查询积分商品详情
     */
    @PostMapping("/pointProductDetail")
    public CommonResult<PointsProductDetailDTO> getpointProductDetail(@RequestBody PointsProductVO pointsProductVO) {
        //判断是否领取过该礼券,领取过不能重复领取

        PointsProductDetailDTO pointsProductDetailDTO = pointsProductService.getpointProductDetail(pointsProductVO);

        return success(pointsProductDetailDTO);
    }



    /**
     * 删除积分商品
     */
//    @Log(title = "积分商品", businessType = BusinessType.DELETE,systemType = SystemType.HBGC)
    @PostMapping("/delete/{productIds}")
    public CommonResult<Integer> remove(@PathVariable Long productIds) {
        return CommonResult.success(pointsProductService.deletePointsProductByProductIds(productIds));
    }
}
