package com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponChooseDO;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.*;
import com.htyoudao.youdao.module.promotion.service.advertising.carousel.CouponChooseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 广告配置轮播图 选择优惠券条件下拉列表")
@RestController
@RequestMapping("/promotion/selector")
public class CouponChooseSelectorController {

    @Operation(summary = "pc端获取选择优惠券适用区域")
    @PostMapping("/getIsCommon")
    public CommonResult<String> getIsCommon(){
        return success(CouponISCommonEnum.getEle());
    }

    @Operation(summary = "pc端获取选择优惠券使用商品")
    @PostMapping("/getIsCommonStore")
    public CommonResult<String> getIsCommonStore(){
        return success(CouponISCommonStoreEnum.getEle());
    }

    @Operation(summary = "pc端获取选择优惠券优惠券类型")
    @PostMapping("/getType")
    public CommonResult<String> getType(){
        return success(CouponTypeEnum.getEle());
    }

    @Operation(summary = "pc端获取选择优惠券用餐方式")
    @PostMapping("/getHabit")
    public CommonResult<String> getHabit(){
        return success(HabitEnum.getEle());
    }

    @Operation(summary = "pc端获取选择优惠券领取限制")
    @PostMapping("/getUserRestrictions")
    public CommonResult<String> getUserRestrictions(){
        return success(UserRestrictionsEnum.getEle());
    }







}
