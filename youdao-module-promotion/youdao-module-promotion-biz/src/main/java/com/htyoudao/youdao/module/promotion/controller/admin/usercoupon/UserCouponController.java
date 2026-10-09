package com.htyoudao.youdao.module.promotion.controller.admin.usercoupon;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UserCouponVO;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponCountRespVo;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponInfoRespVo;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponPageReqVo;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponPageRespVo;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


@Tag(name = "管理端 - 用户优惠券")
@RestController
@RequestMapping("/promotion/user-coupon")
@Validated
public class UserCouponController {

    @Resource
    private UserCouponService userCouponService;


    @GetMapping("/userCouponList")
    @Operation(summary = "领取记录")
    public CommonResult<PageResult<UserCouponPageRespVo>> userCouponList(UserCouponPageReqVo couponPageReqVo) {
        PageResult<UserCouponPageRespVo> userCouponList = userCouponService.userCouponList(couponPageReqVo);
        return CommonResult.success(userCouponList);
    }

    @PostMapping("/exportUserCouponList")
    @Operation(summary = "导出领取记录")
    public CommonResult<Void> exportUserCouponList(@RequestBody UserCouponPageReqVo couponPageReqVo) {
        userCouponService.exportUserCouponList(couponPageReqVo);
        return CommonResult.success(null);
    }


    @GetMapping("/getCount")
    @Operation(summary = "获取数量")
    public CommonResult<UserCouponCountRespVo> getCount(@RequestParam("couponId") Long couponId) {
        UserCouponCountRespVo userCouponCountRespVo= userCouponService.getCount(couponId);
        return CommonResult.success(userCouponCountRespVo);
    }

    /**
     * 会员日发放
     * @return
     */
    @GetMapping("/memberDayCoupon")
    @Operation(summary = "会员日发放")
    @PermitAll
    public void memberDayCoupon(){
        userCouponService.memberDayCoupon();
    }

    /**
     * 会员日发放
     * @return
     */
    @GetMapping("/memberCardBenefitJob")
    @Operation(summary = "会员卡发放")
    public void memberCardBenefitJob(){
        userCouponService.memberCardBenefitJob();
    }




}