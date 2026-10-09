package com.htyoudao.youdao.module.promotion.controller.admin.activityMj;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityDataAnalysisRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityNjnzSaveReqVO;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityMj.ActivityMjService;
import com.htyoudao.youdao.module.promotion.service.activityNjnz.ActivityNjnzService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.MJ_IS_NOT_ID;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.NJNZ_IS_NOT_ID;

/**
 * @author Larkin
 */
@Tag(name = "后台pc - 营销活动满减满折")
@RestController
@RequestMapping("/promotion/activityMj")
public class ActivityMjController {

    @Resource
    private ActivityMjService activityMjService;
    @Resource
    private ActivityService activityService;


    @PostMapping("/create")
    @Operation(summary = "创建满减满折关系")
//    @PreAuthorize("@ss.hasPermission('promotion:activityMj:create')")
    public CommonResult<Boolean> createActivityMj(@Valid @RequestBody ActivityMjSaveReqVO activitySaveReqVO) {
        activityMjService.createActivityMj(activitySaveReqVO);
        return success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "修改满减满折关系")
//    @PreAuthorize("@ss.hasPermission('promotion:activityMj:update')")
    public CommonResult<Boolean> updateActivityMj(@Valid @RequestBody ActivityMjSaveReqVO activitySaveReqVO) {
        if (activitySaveReqVO.getId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        activityMjService.updateActivityMj(activitySaveReqVO);
        return success(true);
    }


    @GetMapping("/delete")
    @Operation(summary = "删除满减满折关系")
//    @PreAuthorize("@ss.hasPermission('promotion:activityMj:delete')")
    public CommonResult<Boolean> deleteActivityMj(@RequestParam(name = "id") Long id) {
        activityMjService.deleteActivityMj(id);
        return success(true);
    }


//    @PreAuthorize("@ss.hasPermission('promotion:activityMj:query')")
    @GetMapping("/selectInfo")
    @Operation(summary = "查询活动详情")
    public CommonResult<ActivityMjInfoRespVO> selectInfo(@RequestParam("id") Long id) {
        ActivityMjInfoRespVO activityInfoRespVO = activityMjService.selectInfo(id);
        return success(activityInfoRespVO);
    }





}
