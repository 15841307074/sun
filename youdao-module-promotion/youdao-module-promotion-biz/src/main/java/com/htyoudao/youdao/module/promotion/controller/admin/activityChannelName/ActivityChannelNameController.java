package com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelNameDataRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannelName.ActivityChannelNameDO;
import com.htyoudao.youdao.module.promotion.service.activityChannelName.ActivityChannelNameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 渠道名称管理")
@RestController
@RequestMapping("/promotion/activity-channel-name")
@Validated
public class ActivityChannelNameController {

    @Resource
    private ActivityChannelNameService activityChannelNameService;

    @PostMapping("/create")
    @Operation(summary = "创建渠道名称")
   // @PreAuthorize("@ss.hasPermission('promotion:activity-channel-name:create')")
    public CommonResult<Long> create(@Valid @RequestBody ActivityChannelNameSaveReqVO reqVO) {
        return success(activityChannelNameService.create(reqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新渠道名称")
  //  @PreAuthorize("@ss.hasPermission('promotion:activity-channel-name:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ActivityChannelNameSaveReqVO reqVO) {
        activityChannelNameService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除渠道名称")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
  //  @PreAuthorize("@ss.hasPermission('promotion:activity-channel-name:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") @NotNull(message = "id不能为空") Long id) {
        activityChannelNameService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得渠道名称")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
  //  @PreAuthorize("@ss.hasPermission('promotion:activity-channel-name:query')")
    public CommonResult<ActivityChannelNameRespVO> get(@RequestParam("id") Long id) {
        ActivityChannelNameDO data = activityChannelNameService.get(id);
        return success(BeanUtils.toBean(data, ActivityChannelNameRespVO.class));
    }

    @PostMapping("/page")
    @Operation(summary = "获得渠道名称分页")
   // @PreAuthorize("@ss.hasPermission('promotion:activity-channel-name:query')")
    public CommonResult<PageResult<ActivityChannelNameRespVO>> page(@Valid @RequestBody ActivityChannelNamePageReqVO reqVO) {
        PageResult<ActivityChannelNameDO> pageResult = activityChannelNameService.page(reqVO);
        return success(BeanUtils.toBean(pageResult, ActivityChannelNameRespVO.class));
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "渠道启用/禁用")
   // @PreAuthorize("@ss.hasPermission('promotion:activity-channel-name:update')")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody ActivityChannelNameUpdateStatusReqVO reqVO) {
        activityChannelNameService.updateStatus(reqVO.getId(), reqVO.getIsEnable());
        return success(true);
    }


    @GetMapping("/getChannelList")
    @Operation(summary = "获取渠道列表")
    public CommonResult<List<ActivityChannelNameDataRespVO>> getChannelList() {
        List<ActivityChannelNameDataRespVO> voList = activityChannelNameService.getChannelList();
        return success(voList);
    }


}

