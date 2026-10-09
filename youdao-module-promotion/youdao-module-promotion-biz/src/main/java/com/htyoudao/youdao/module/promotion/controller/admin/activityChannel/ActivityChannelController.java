package com.htyoudao.youdao.module.promotion.controller.admin.activityChannel;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityChannel.vo.ActivityChannelReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 活动推广渠道")
@RestController
@RequestMapping("/promotion/activity-channel")
@Validated
public class ActivityChannelController {

    @Resource
    private ActivityChannelService activityChannelService;

    @PostMapping("/refresh")
    @Operation(summary = "点击刷新拉取新的渠道")
    public CommonResult<Boolean> refresh(@Valid @RequestBody ActivityChannelReqVO reqVO) {
        activityChannelService.refreshByActivityId(reqVO.getActivityId(),reqVO.getChannelIds(),reqVO.getType());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取活动推广渠道列表")
    public CommonResult<List<ActivityChannelRespVO>> list(@RequestParam Long activityId) {
        List<ActivityChannelDO> activityChannelDOList = activityChannelService.selectByActivityIdWithIsEnable(activityId);
        List<ActivityChannelRespVO> list = new ArrayList<>();

        activityChannelDOList.forEach(activityChannelDO -> {
            ActivityChannelRespVO activityChannelRespVO = new ActivityChannelRespVO();
            BeanUtils.copyProperties(activityChannelDO, activityChannelRespVO);
            list.add(activityChannelRespVO);
        });
        return success(list);
    }


    @GetMapping("/initialization")
    @Operation(summary = "初始化渠道")
    public CommonResult<Boolean> initialization() {
        activityChannelService.initialization();
        return success(true);
    }



}
