package com.htyoudao.youdao.module.promotion.controller.app.usercoupon;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.commodity.api.DTO.ItemDto;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.ClaimTiktokCouponReqVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UsedCouponReqVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UserCouponVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.*;
import com.htyoudao.youdao.module.promotion.framework.config.properties.RepeatSubmit;
import com.htyoudao.youdao.module.promotion.job.usercoupon.UserCouponJob;
import com.htyoudao.youdao.module.promotion.service.couponcommodity.CouponCommodityService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.ratelimit.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.CLAIM_COUPON_LIMITER;

/**
 * @author dht
 */
@Tag(name = "app - 用户优惠券")
@RestController
@RequestMapping("/promotion/app-user-coupon")
@Validated
public class UserCouponForAppController {

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private CouponCommodityService couponCommodityService;

    @Resource
    private GoodCouponService goodCouponService;

    @Resource
    private RateLimitService rateLimitService;

    @GetMapping("/getCountNum")
    @Operation(summary = "我的优惠券数量  需要开放鉴权")
    @Parameter(name = "userId", description = "用户id", required = true)
    //@Parameter(name = "couponStatus", description = "状态", required = true)
    @Parameter(name = "isUsed", description = "是否使用", required = true)
    @DataPermission(enable = false)
    public CommonResult<Long> getCountNum(@RequestParam(value = "userId") long userId,
                                   //@RequestParam(value = "couponStatus") int couponStatus,
                                   @RequestParam(value = "isUsed") int isUsed) {
        return success(userCouponService.getCountNum(userId, isUsed));
    }

    @PostMapping("/myCouponList")
    @Operation(summary = "我的优惠券列表")
    public CommonResult<PageResult<AppCouponListRespVO>> myCouponList(@RequestBody AppCouponListReqVO appCouponListReqVO) {
        //根据userId  得到couponID   user_coupon
        PageResult<AppCouponListRespVO> page = userCouponService.selectUserCouponListByUserId(appCouponListReqVO);
        List<AppCouponListRespVO> records = page.getList();
        if (CollectionUtil.isNotEmpty(records)) {
            if (ObjectUtil.isNotEmpty(appCouponListReqVO) && ObjectUtil.isNotEmpty(appCouponListReqVO.getIsUsed()) && appCouponListReqVO.getIsUsed() == 1) {
                records.sort((e1, e2) -> e2.getUseTime().compareTo(e1.getUseTime()));
            }
        }
        return success(page);
    }

    @PostMapping("/getCouponByUserAndCouponId")
    @Operation(summary = "小程序查询优惠券详情")
    public CommonResult<UserOneCouponRespVO> getCouponByUserAndCouponId(@RequestBody UserCouponReqVO userCouponReqVO) {
        return success(userCouponService.getCouponByUserAndCouponId(userCouponReqVO));
    }


    @DeleteMapping("/delOverdueCoupon")
    @Operation(summary = "优惠卷过期删除")
    public void delOverdueCoupon(){
        userCouponService.delOverdueCoupon();
    }

    /**
     *
     * @param usedCouponReqVO usedCouponReqVO
     * @return Boolean
     */
    @PutMapping("/usedCoupon")
    @Operation(summary = "使用/回退优惠券")
    public CommonResult<Boolean> usedCoupon(@RequestBody UsedCouponReqVO usedCouponReqVO) {
        userCouponService.usedCoupon(usedCouponReqVO);
        return success(Boolean.TRUE);
    }

    /**
     * 结算时查询用户优惠券列表(按优惠力度)
     */
    @PostMapping("/settlement/couponList")
    @Operation(summary = "结算时查询用户优惠券列表")
    public CommonResult<AppUserCouponRespVO> couponList(@RequestBody SettlementReqVO settlementReqVO) {
        return success(userCouponService.getCouponList(settlementReqVO));
    }


    @PostMapping("/claimOneCoupon")
    @Operation(summary = "领取优惠券（分享页面）")
    public CommonResult<Boolean> claimOneCoupon(@RequestBody ClaimCouponReqVO claimCoupon) {
        boolean b = rateLimitService.allowClaimCouponRequest(claimCoupon.getMemberId());
        if(!b){
            throw exception(CLAIM_COUPON_LIMITER);
        }
        return success(userCouponService.claimOneCoupon(claimCoupon));
    }

    @PostMapping("/claimCoupon")
    @Operation(summary = "在积分商品页面领取优惠券")
    @RepeatSubmit
    public CommonResult<Boolean> claimCouponWithProduct(@RequestBody ClaimCouponReqVO claimCoupon) {
        return success(userCouponService.claimCouponWithProduct(claimCoupon));
    }


    @PostMapping("/claimTiktokCoupon")
    @Operation(summary = "领取抖音优惠券 后端用")
    public CommonResult<Long> claimTiktokCoupon(@RequestBody ClaimTiktokCouponReqVO claimCoupon) {
        return success(userCouponService.claimTiktokCoupon(claimCoupon));
    }

    @GetMapping("/selectCouponData")
    @Operation(summary = "获取优惠卷详情-远程调用")
    public List<UserCouponVO> selectCouponData(@RequestParam("memberId") Long memberId) {
        return userCouponService.selectCouponData(memberId);
    }


    @GetMapping("/selectCouponCommodity")
    @Operation(summary = "获取优惠卷商品详情")
    public CommonResult<List<AppCouponCommodityVO>> selectCouponCommodity(@RequestParam("couponId") Long couponId) {
        return success(couponCommodityService.selectCouponCommodity(couponId));
    }

    @GetMapping("/getAppCouponById")
    @Operation(summary = "优惠券详情 需要开放鉴权")
    @DataPermission(enable = false)
    @Parameter(name = "id", description = "优惠券id", required = true, example = "1024")
    public CommonResult<GoodCouponRespVO> getCoupon(@RequestParam("id") Long id) {
        return success(goodCouponService.getAppCouponById(id));
    }

    @GetMapping("/judgmentTime")
    @Operation(summary = "优惠券是否在领取时间 需要开放鉴权")
    @DataPermission(enable = false)
    @Parameter(name = "id", description = "优惠券id", required = true, example = "1024")
    public CommonResult<Boolean> judgmentTime(@RequestParam("id") Long id) {
        return success(goodCouponService.judgmentTime(id));
    }

    /**
     *  商品兑换卷去使用
     */
    @GetMapping("/couponIsUsed")
    @Operation(summary = "商品兑换卷去使用")
    public CommonResult<ItemDto> couponIsUsed(@RequestParam(value = "couponId") Long couponId, @RequestParam(value = "storeId") Long storeId) {
        return CommonResult.success(userCouponService.couponIsUsed(couponId,storeId));
    }

    @PostMapping("/claimSeckillCoupon")
    @Operation(summary = "领取优惠券（秒杀页面）")
    public CommonResult<Boolean> claimSeckillCoupon(@RequestBody ClaimCouponReqVO claimCoupon) {
//        boolean b = rateLimitService.allowClaimCouponRequest(claimCoupon.getMemberId());
//        if(!b){
//            throw exception(CLAIM_COUPON_LIMITER);
//        }
        return success(userCouponService.claimSeckillCoupon(claimCoupon));
    }

    @PostMapping("/claimPointsCoupon")
    @Operation(summary = "领取优惠券（集点页面）")
    public CommonResult<Boolean> claimPointsCoupon(@RequestBody ClaimPointsCouponReqVO claimCoupon) {
        boolean b = rateLimitService.allowClaimCouponJDRequest(claimCoupon.getMemberId(),claimCoupon.getCouponId());
        if(!b){
            throw exception(CLAIM_COUPON_LIMITER);
        }
        return success(userCouponService.claimPointsCoupon(claimCoupon));
    }

}