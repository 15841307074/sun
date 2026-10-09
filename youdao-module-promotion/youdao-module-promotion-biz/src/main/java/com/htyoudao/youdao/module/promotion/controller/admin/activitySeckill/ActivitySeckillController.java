package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;


import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.*;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.ActivitySeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 秒杀活动")
@RestController
@RequestMapping("/promotion/activity-seckill")
@RequiredArgsConstructor
public class ActivitySeckillController {



    @Resource
    private ActivitySeckillService activitySeckillService;



    @PostMapping("/create")
    @Operation(summary = "创建秒杀活动")
//    @PreAuthorize("@ss.hasPermission('promotion:activitySeckill:create')")
    public CommonResult<Boolean> createActivitySeckill(@Valid @RequestBody ActivitySeckillReqSaveVO  activitySeckillReqSaveVO) {
        activitySeckillService.createActivitySeckill(activitySeckillReqSaveVO);
        return success(true);
    }


    @PostMapping("/update")
    @Operation(summary = "修改秒杀活动")
//    @PreAuthorize("@ss.hasPermission('promotion:activitySeckill:update')")
    public CommonResult<Boolean> updateActivitySeckill(@Valid @RequestBody ActivitySeckillReqSaveVO  activitySeckillReqSaveVO) {
        activitySeckillService.updateActivitySeckill(activitySeckillReqSaveVO);
        return success(true);
    }

    @GetMapping("/selectInfo")
    @Operation(summary = "查寻秒杀活动详情")
//    @PreAuthorize("@ss.hasPermission('promotion:activitySeckill:query')")
    public CommonResult<ActivitySeckillRespVO> selectInfo(@RequestParam("id") Long id) {
        ActivitySeckillRespVO  activitySeckillRespVO = activitySeckillService.selectInfo(id);

        return success(activitySeckillRespVO);
    }

    @PostMapping("/updateSpread")
    @Operation(summary = "修改秒杀活动的推广")
//    @PreAuthorize("@ss.hasPermission('promotion:activitySeckillSpread:update')")
    public CommonResult<Boolean> updateSpread(@Valid @RequestBody ActivitySeckillSpreadSaveReqVO  activitySeckillSpreadReqVO) {
        activitySeckillService.updateSpread(activitySeckillSpreadReqVO);
        return success(true);
    }


    @GetMapping("/selectSpread")
    @Operation(summary = "查询秒杀活动的推广")
 //   @PreAuthorize("@ss.hasPermission('promotion:activitySeckillSpread:query')")
    public CommonResult<ActivitySeckillSpreadRespVO> selectSpread(@RequestParam("id") Long id) {
        ActivitySeckillSpreadRespVO activitySeckillSpreadRespVO = activitySeckillService.selectSpread(id);
        return success(activitySeckillSpreadRespVO);
    }

    /**
     * 改活动状态给冬冬
     * @param activitySeckillEnabledUpdateReqVO
     * @return
     */
//   @PreAuthorize("@ss.hasPermission('promotion:activitySeckillEnabled:update')")
    @PostMapping("/updateEnabled")
    @Operation(summary = "修改秒杀活动的开启状态")
    public CommonResult<Boolean> updateEnabled(@Valid @RequestBody ActivitySeckillEnabledUpdateReqVO  activitySeckillEnabledUpdateReqVO) {
        activitySeckillService.updateEnabled(activitySeckillEnabledUpdateReqVO);
        return success(true);

    }
 //    @PreAuthorize("@ss.hasPermission('promotion:activitySeckill:delete')")
    @DeleteMapping("/delete")
    @Operation(summary = "删除秒杀活动")
    public CommonResult<Boolean> deleteActivitySeckill(@RequestParam("id") Long id) {

        activitySeckillService.deleteActivitySeckill(id);

        return success(true);
    }





}
