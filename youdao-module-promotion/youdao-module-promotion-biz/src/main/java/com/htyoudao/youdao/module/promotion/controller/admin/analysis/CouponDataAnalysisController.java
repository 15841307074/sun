package com.htyoudao.youdao.module.promotion.controller.admin.analysis;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChoosedReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.CouponDataAnalysisReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.CouponDataAnalysisRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.CouponDataDownloadReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponChooseDO;
import com.htyoudao.youdao.module.promotion.service.advertising.carousel.CouponChooseService;
import com.htyoudao.youdao.module.promotion.service.analysis.CouponDataAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 优惠券数据分析")
@RestController
@RequestMapping("/promotion/couponDataAnalysis")
public class CouponDataAnalysisController {


    @Resource
    private CouponDataAnalysisService couponDataAnalysisService;


    @Operation(summary = "pc端获取优惠券分析")
    @GetMapping("/get/{id}")
    public CommonResult<CouponDataAnalysisRespVO> getData(@PathVariable("id") Long id){
        CouponDataAnalysisRespVO couponDataAnalysisRespVO = couponDataAnalysisService.getDataAnalysis(id);
        return success(couponDataAnalysisRespVO);
    }


    @Operation(summary = "pc端按照门店获取优惠券分析")
    @PostMapping("/getByStoreAndOrg")
    public CommonResult<PageResult<CouponDataAnalysisRespVO>> getDataByStoreAndOrg(@Validated @RequestBody CouponDataAnalysisReqVO couponDataAnalysisReqVO){
        PageResult<CouponDataAnalysisRespVO> dataByStoreAndOrg = couponDataAnalysisService.getDataByStoreAndOrg(couponDataAnalysisReqVO);
        return success(dataByStoreAndOrg);
    }

    @Operation(summary = "pc端导出按照门店获取优惠券分析")
    @PostMapping("/exportListByStoreAndOrg")
    public CommonResult<Void> exportListByStoreAndOrg(@Validated @RequestBody CouponDataAnalysisReqVO couponDataAnalysisReqVO){
        couponDataAnalysisService.exportListByStoreAndOrg(couponDataAnalysisReqVO);
        return success(null);
    }

    @Operation(summary = "pc端导出按照门店获取优惠券分析")
    @PostMapping("/queryCouponData")
    public CommonResult<List<CouponDataAnalysisRespVO>> queryCouponData(@Validated @RequestBody CouponDataAnalysisReqVO couponDataAnalysisReqVO){
        return success(couponDataAnalysisService.queryCouponData(couponDataAnalysisReqVO));
    }

    @Operation(summary = "pc端按照渠道获取优惠券分析")
    @GetMapping("/getBySource/{id}")
    public CommonResult<PageResult<CouponDataAnalysisRespVO>> getDataBySource(@PathVariable("id") Long id){
        PageResult<CouponDataAnalysisRespVO> dataByStoreAndOrg = couponDataAnalysisService.getDataBySource(id);
        return success(dataByStoreAndOrg);
    }

    @Operation(summary = "优惠券使用记录汇总")
    @PostMapping("couponDataDownload")
    public CommonResult<Void> couponDateDownload(@Validated @RequestBody CouponDataDownloadReqVO couponDataDownloadReqVO){
        couponDataAnalysisService.couponDataDownload(couponDataDownloadReqVO);
        return success(null);
    }

    @Operation(summary = "按渠道分析汇总")
    @PostMapping("couponDataDownloadBySource")
    public CommonResult<Void> couponDataDownloadV2(@Validated @RequestBody CouponDataDownloadReqVO couponDataDownloadReqVO){
        couponDataAnalysisService.couponDataDownloadBySource(couponDataDownloadReqVO);
        return success(null);
    }

}
