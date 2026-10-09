package com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChoosedReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponPageRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponChooseDO;
import com.htyoudao.youdao.module.promotion.service.advertising.carousel.CouponChooseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 广告配置轮播图 选择优惠券")
@RestController
@RequestMapping("/promotion/couponChoose")
public class CouponChooseController {


    @Resource
    private CouponChooseService couponChooseService;
    @Operation(summary = "pc端获取选择优惠券")
    @PostMapping("/getAll")
    public CommonResult<PageResult<CouponChooseRespVO>> getList(@Valid @RequestBody(required = false) CouponChooseReqVO couponChooseReqVO){
        PageResult<CouponChooseDO> chooseDOPageResult = couponChooseService.getPage(couponChooseReqVO);
        return success(BeanUtils.toBean(chooseDOPageResult, CouponChooseRespVO.class));
    }

    @Operation(summary = "pc端获取已选择优惠券")
    @PostMapping("/getChoose")
    public CommonResult<CouponChooseRespVO> getChoose(@Valid @RequestBody(required = false) CouponChoosedReqVO couponChoosedReqVO){
        CouponChooseDO chooseDO = couponChooseService.getChoose(couponChoosedReqVO);
        return success(BeanUtils.toBean(chooseDO, CouponChooseRespVO.class));
    }


    /**
     * 查询优惠券列表
     */
    @PostMapping("/asyncCouponPage")
    @Operation(summary = "数据分析的优惠券列表")
    public CommonResult<PageResult<CouponChooseDO>> asyncCouponPage(@Valid @RequestBody(required = false) CouponChooseReqVO couponChooseReqVO) {
        return success(couponChooseService.asyncPage(couponChooseReqVO));
    }



}
