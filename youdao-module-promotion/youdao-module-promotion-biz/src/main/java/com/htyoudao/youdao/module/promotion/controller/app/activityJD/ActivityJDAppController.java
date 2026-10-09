package com.htyoudao.youdao.module.promotion.controller.app.activityJD;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo.ActPointRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo.ActivityCollectAppShareVO;
import com.htyoudao.youdao.module.promotion.controller.app.activitySeckill.vo.ActivitySeckillAppShareVO;
import com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 集点活动")
@RestController
@RequestMapping("/promotion/app/activity-jd")
@RequiredArgsConstructor
public class ActivityJDAppController {

    @Resource
    private ActivityJDService activityJDService;

    @GetMapping("/getShareVO")
    @Operation(summary = "小程序秒杀活动分享详情")
    @PermitAll
    public CommonResult<ActivityCollectAppShareVO> getShareVO(@RequestParam("activityId") Long activityId){
        ActivityCollectAppShareVO activityCollectAppShareVO =   activityJDService.getShareVO(activityId);
        return success(activityCollectAppShareVO);
    }

    @GetMapping("/getPointsDetail")
    @Operation(summary = "小程序集点获取活动详情")
    @PermitAll
    public CommonResult<ActPointRespVO> getPointsDetail(@RequestParam(value = "activityId") Long activityId,
                                                        @RequestParam(value = "memberId", required = false) Long memberId){
        ActPointRespVO actPointRespVO = activityJDService.getPointsDetail(activityId,memberId);
        return success(actPointRespVO);
    }

    @GetMapping("/incrMemberPointsByMe")
    @Operation(summary = "手动给人加点")
    @PermitAll
    public CommonResult<Boolean> incrMemberPointsByMe(@RequestParam(value = "activityId") Long activityId,
                                                            @RequestParam(value = "memberId", required = false) Long memberId,
                                                             @RequestParam(value = "points") Integer points){
        activityJDService.incrMemberPointsByMe(activityId,memberId,points);
        return success(Boolean.TRUE);
    }


}
