package com.htyoudao.youdao.module.commodity.controller.admin.activity;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.CommodityActivityPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.CommodityActivityRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.CommodityActivitySaveReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.activity.CommodityActivityDO;
import com.htyoudao.youdao.module.commodity.service.activity.CommodityActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "管理后台 - 商品活动")
@RestController
@RequestMapping("/commodity/activity")
//@Validated
public class CommodityActivityController {

    @Resource
    private CommodityActivityService activityService;

    @PostMapping("/create")
    @Operation(summary = "创建活动商品")
    @PreAuthorize("@ss.hasPermission('commodity:activity:create')")
    public CommonResult<Boolean> createActivity(@Valid @RequestBody CommodityActivitySaveReqVO createReqVO) {
        return success(activityService.createActivity(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除活动商品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('commodity:activity:delete')")
    public CommonResult<Boolean> deleteActivity(@RequestParam("id") Long activityId) {
        activityService.deleteActivity(activityId);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得活动商品分页")
    //@PreAuthorize("@ss.hasPermission('commodity:activity:query')")
    public CommonResult<PageResult<CommodityActivityRespVO>> getActivityPage(@Valid CommodityActivityPageReqVO pageReqVO) {
        PageResult<CommodityActivityDO> pageResult = activityService.getActivityPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CommodityActivityRespVO.class));
    }

    /**
     * 查询活动商品
     * @return CommonResult<List<CommodityActivityRespVO>>
     */
    @GetMapping("/listForCommodity")
    @Operation(summary = "抽奖商品下拉")
    @PreAuthorize("@ss.hasPermission('commodity:activity:create')")
    public CommonResult<List<CommodityActivityRespVO>> listForCommodity(){
        List<CommodityActivityRespVO> list = activityService.selcetSpusForActivity();
        return success(list);

    }

}