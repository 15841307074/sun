package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.GoodCouponPackagePageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.GoodCouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponPageReqVo;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponPageRespVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "管理后台 - 优惠券包")
@RestController
@RequestMapping("/promotion/coupon-package")
@Validated
public class CouponPackageController {

    @Resource
    private CouponPackageService couponPackageService;

    @PostMapping("/create")
    @Operation(summary = "创建优惠券包关系")
    @PreAuthorize("@ss.hasPermission('package:coupon-package:create')")
    public CommonResult<Boolean> createCouponPackage(@Valid @RequestBody CouponPackageSaveReqVO createReqVO) {
        return success(couponPackageService.insert(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新优惠券包关系")
    @PreAuthorize("@ss.hasPermission('package:coupon-package:update')")
    public CommonResult<Boolean> updateCouponPackage(@Valid @RequestBody CouponPackageSaveReqVO updateReqVO) {
        return success(couponPackageService.update(updateReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除优惠券包关系")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('package:coupon-package:delete')")
    public CommonResult<Boolean> deleteCouponPackage(@RequestParam("id") Long id) {
        return null;
    }

    @GetMapping("/get")
    @Operation(summary = "获得优惠券包关系")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('package:coupon-package:info')")
    public CommonResult<GoodCouponPackageRespVO> getCouponPackage(@RequestParam("id") Long id) {
        return null;
    }

    @GetMapping("/page")
    @Operation(summary = "获得优惠券包关系分页")
    @PreAuthorize("@ss.hasPermission('package:coupon-package:page')")
    public CommonResult<PageResult<GoodCouponPackageRespVO>> getCouponPackagePage(@Valid GoodCouponPackagePageReqVO pageReqVO) {
        return null;
    }

    @GetMapping("/getPackagePage")
    @Operation(summary = "获得优惠卷包分页")
    //@PreAuthorize("@ss.hasPermission('coupon-package:query')")
    public CommonResult<PageResult<CouponPackageRespVO>>  getPageCouponPackage(@Valid GetCouponPackagePageReqVO pageReqVO) {
       return success(couponPackageService.getPageCouponPackage(pageReqVO));
    }

    @GetMapping("/getPackageById")
    @Operation(summary = "查询优惠券包详情")
    @DataPermission(enable = false)
    @PreAuthorize("@ss.hasPermission('package:coupon-package:info')")
    public CommonResult<CouponPackageRespVO> getById(@RequestParam(value = "id", required = false) Long id) {
        CouponPackageRespVO couponPackageVO = couponPackageService.selectById(id);
        return success(couponPackageVO);
    }

    @DeleteMapping("/deletePackage")
    @Operation(summary = "删除优惠券包")
    @PreAuthorize("@ss.hasPermission('package:coupon-package:delete')")
    public CommonResult<Boolean> delete(@RequestBody CouponPackageDelVO couponPackageDelVO) {
        couponPackageService.removeById(couponPackageDelVO.getId());
        return success(true);
    }

    /**
     * 修改优惠券包数量
     */
    @PutMapping("/editPackageNum")
    @Operation(summary = "修改优惠券包数量")
    @PreAuthorize("@ss.hasPermission('package:coupon-package:update')")
    public CommonResult<Boolean> editPackageNum(@RequestBody CouponPackageUpdateNumVO couponPackageAO) {
        couponPackageService.editPackageNum(couponPackageAO);
        return success(true);
    }

    /**
     * 优惠券包上下架
     *
     * @param
     * @return
     */
    @PutMapping("/updateIsGround")
    @Operation(summary = "优惠券包上下架")
    @PreAuthorize("@ss.hasPermission('package:coupon-package:updateIsGround')")
    public CommonResult<Boolean> updateIsGround(@RequestBody CouponPackageUpdateGroundVO couponPackageUpdateGroundVO) {
        couponPackageService.updateIsGround(couponPackageUpdateGroundVO);
        return success(true);
    }

    @PostMapping("/nocPackageStoreNum")
    @Operation(summary = "定时同步优惠券包门店领取数量")
    public CommonResult<Boolean> nocCouponStoreNum() {
        couponPackageService.nocCouponStoreNum();
        return CommonResult.success(true);
    }

    @GetMapping("/couponPackageList")
    @Operation(summary = "优惠卷包-领取记录")
    public CommonResult<PageResult<CouponPackageCollectRespVO>> couponPackageList(CouponPackageCollectReqVO collectReqVO) {
        PageResult<CouponPackageCollectRespVO> userCouponList = couponPackageService.couponPackageList(collectReqVO);
        return CommonResult.success(userCouponList);
    }


    @GetMapping("/couponPackageInfo")
    @Operation(summary = "优惠卷包-详情")
    public CommonResult<CouponPackageDO> couponPackageInfo(@RequestParam("id")Long id) {
        CouponPackageDO couponPackageDO = couponPackageService.couponPackageInfo(id);
        return CommonResult.success(couponPackageDO);
    }


    @GetMapping("/getCouponPackageCount")
    @Operation(summary = "优惠卷包-是否已领取数量")
    public CommonResult<CouponPackageCountRespVo> getCouponPackageCount(@RequestParam("id")Long id) {
        CouponPackageCountRespVo couponPackageDO = couponPackageService.getCouponPackageCount(id);
        return CommonResult.success(couponPackageDO);
    }

    @PutMapping("/updateAllH5")
    @Operation(summary = "更新所有h5")
    @PermitAll
    @DataPermission
    public CommonResult<Integer> updateAllH5() {
        return success(couponPackageService.updateAllH5());
    }

    @GetMapping("/getCommunityQrImage")
    @Operation(summary = "获取社群二维码")
    public CommonResult<List<String>> getCommunityQrImage(Long couponId) {
        return success(couponPackageService.getCommunityQrImage(couponId));
    }
}