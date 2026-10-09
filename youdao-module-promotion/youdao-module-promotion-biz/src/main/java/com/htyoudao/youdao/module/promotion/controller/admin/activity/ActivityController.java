package com.htyoudao.youdao.module.promotion.controller.admin.activity;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activity.vo.ActivityDataRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityPageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityUpdateStatusReqVO;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "后台pc - 活动")
@RestController
@RequestMapping("/promotion/activity")
public class ActivityController {

    @Resource
    private ActivityService activityService;

    @Operation(summary = "活动分页列表页面")
    @PostMapping("/page")
    public CommonResult<PageResult<ActivityPageRespVO>> getList(@RequestBody(required = false) ActivityPageReqVO activityPageReqVO) {
        PageResult<ActivityPageRespVO> activityPageRespVOPageResult = activityService.getPage(activityPageReqVO);
        return CommonResult.success(activityPageRespVOPageResult);
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "修改上下架状态")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody ActivityUpdateStatusReqVO reqVO) {
        activityService.updateStatus(reqVO.getId(), reqVO.getIsEnabled());
        return success(true);
    }


    @PostMapping("/selectActivityList")
    @Operation(summary = "获取活动列表（下拉）")
    public CommonResult<PageResult<ActivityPageRespVO>> selectActivityList(@RequestBody(required = false) ActivityPageReqVO activityPageReqVO) {
        PageResult<ActivityPageRespVO> pageResult = activityService.selectActivityList(activityPageReqVO);
        return success(pageResult);
    }

}
