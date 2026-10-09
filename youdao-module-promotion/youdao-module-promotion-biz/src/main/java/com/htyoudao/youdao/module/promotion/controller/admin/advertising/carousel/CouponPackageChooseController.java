package com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponPackageChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponPackageChooseRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponPackageChoosedReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponPackageChooseDO;
import com.htyoudao.youdao.module.promotion.service.advertising.carousel.CouponPackageChooseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 广告配置轮播图 选择优惠券包")
@RestController
@RequestMapping("/promotion/couponPackageChoose")
public class CouponPackageChooseController {


    @Resource
    private CouponPackageChooseService couponPackageChooseService;
    @Operation(summary = "pc端获取选择优惠券包")
    @PostMapping("/getAll")
    public CommonResult<PageResult<CouponPackageChooseRespVO>> getList(@Valid @RequestBody(required = false) CouponPackageChooseReqVO couponPackageChooseReqVO){
        PageResult<CouponPackageChooseDO> chooseDOPageResult = couponPackageChooseService.getPage(couponPackageChooseReqVO);
        return success(BeanUtils.toBean(chooseDOPageResult, CouponPackageChooseRespVO.class));
    }

    @Operation(summary = "pc端获取选择优惠券包")
    @PostMapping("/getChoose")
    public CommonResult<CouponPackageChooseRespVO> getChoose(@Valid @RequestBody(required = false) CouponPackageChoosedReqVO couponPackageChoosedReqVO){
        CouponPackageChooseDO chooseDOPageResult = couponPackageChooseService.getChoose(couponPackageChoosedReqVO);
        return success(BeanUtils.toBean(chooseDOPageResult, CouponPackageChooseRespVO.class));
    }



}
