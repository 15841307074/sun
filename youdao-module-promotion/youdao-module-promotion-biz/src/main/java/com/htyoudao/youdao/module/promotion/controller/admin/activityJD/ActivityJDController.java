package com.htyoudao.youdao.module.promotion.controller.admin.activityJD;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDFullRespVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDSpreadRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.*;
import com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDService;
import com.htyoudao.youdao.module.promotion.service.job.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 集点活动")
@RestController
@RequestMapping("/promotion/activity-jd")
@RequiredArgsConstructor
public class ActivityJDController {



    @Resource
    private ActivityJDService activityJDService;

    @Resource
    private JobService jobService;



    @PostMapping("/create")
    @Operation(summary = "创建集点活动")
    public CommonResult<Boolean> createActivityJD(@Valid @RequestBody ActivityJDReqSaveVO activityJDReqSaveVO) {
        activityJDService.createActivityJD(activityJDReqSaveVO);
        return success(true);
    }

    @GetMapping("/selectSpread")
    @Operation(summary = "查询集点活动的推广")
    public CommonResult<ActivityJDSpreadRespVO> selectSpread(@RequestParam("id") Long id) {
        ActivityJDSpreadRespVO activityJDSpreadRespVO = activityJDService.selectSpread(id);
        return success(activityJDSpreadRespVO);
    }

    @PostMapping("/updateSpread")
    @Operation(summary = "修改集点活动的推广")
//    @PreAuthorize("@ss.hasPermission('promotion:activitySeckillSpread:update')")
    public CommonResult<Boolean> updateSpread(@Valid @RequestBody ActivityJDSpreadSaveReqVO activityJDSpreadSaveReqVO) {
        activityJDService.updateSpread(activityJDSpreadSaveReqVO);
        return success(true);
    }



    @PostMapping("/update")
    @Operation(summary = "修改集点活动")
    public CommonResult<Boolean> updateActivityJD(@Valid @RequestBody ActivityJDReqSaveVO  activityJDReqSaveVO) {
        activityJDService.updateActivityJD(activityJDReqSaveVO);
        return success(true);
    }

    @GetMapping("/selectInfo")
    @Operation(summary = "查询集点活动详情")
    public CommonResult<ActivityJDFullRespVO> selectInfo(@RequestParam("id") Long id) {
        ActivityJDFullRespVO activityJDFullRespVO = activityJDService.selectFullInfo(id);

        return success(activityJDFullRespVO);
    }
    @PostMapping("/updateEnabled")
    @Operation(summary = "修改集点活动的开启状态")
    public CommonResult<Boolean> updateEnabled(@Valid @RequestBody ActivityJDEnabledUpdateReqVO  activityJDEnabledUpdateReqVO) {
        activityJDService.updateEnabled(activityJDEnabledUpdateReqVO);
        return success(true);

    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除集点活动")
    public CommonResult<Boolean> deleteActivityJD(@RequestParam("id") Long id) throws IOException {

        activityJDService.deleteActivityJD(id);

        return success(true);
    }

//    @GetMapping("/testCoupon")
    @Operation(summary = "测试集点活动")
    public void testCoupon(@RequestParam("id") Long id) throws IOException {

        activityJDService.updateCoupon(id);

    }

//    @GetMapping("/testCouponPackage")
    @Operation(summary = "测试集点活动")
    public void testCouponPackage(@RequestParam("id") Long id) throws IOException {

        activityJDService.updateCouponPackage(id);

    }

    @PostMapping("/automaticPointsGoodsJobHandler")
    @Operation(summary = "试验一下xxjob")
    public CommonResult<Boolean> automaticPointsGoodsJobHandler() {
        jobService.automaticPointsGoodsJobHandler();
        return success(true);

    }
    @GetMapping("/createUrl")
    @Operation(summary = "sssss")
    public CommonResult<Boolean> createUrl( String sortPath) {
        activityJDService.createUrl(10L,sortPath);
        return success(true);
    }

}
