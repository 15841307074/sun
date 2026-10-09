package com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.NJNZ_IS_NOT_ID;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityDataAnalysisRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityNjnzSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityPageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityUpdateStatusReqVO;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityNjnz.ActivityNjnzService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author villky
 */
@Tag(name = "后台pc - 营销活动n件n折")
@RestController
@RequestMapping("/promotion/activityNJNZ")
public class ActivityNjnzController {

    @Resource
    private ActivityNjnzService activityNjnzService;
    @Resource
    private ActivityService activityService;


    @PostMapping("/create")
    @Operation(summary = "创建njnz关系")
    @PreAuthorize("@ss.hasPermission('promotion:activityNJNZ:create')")
    public CommonResult<Boolean> createActivityNjnz(@Valid @RequestBody ActivityNjnzSaveReqVO activitySaveReqVO) {
        activityNjnzService.createActivityNjnz(activitySaveReqVO);
        return success(true);
    }

    @PostMapping("/update")
    @Operation(summary = "修改njnz关系")
    @PreAuthorize("@ss.hasPermission('promotion:activityNJNZ:update')")
    public CommonResult<Boolean> updateActivityNjnz(@Valid @RequestBody ActivityNjnzSaveReqVO activitySaveReqVO) {
        if (activitySaveReqVO.getId() == null) {
            throw exception(NJNZ_IS_NOT_ID);
        }
        activityNjnzService.updateActivityNjnz(activitySaveReqVO);
        return success(true);
    }


    @DeleteMapping("/delete")
    @Operation(summary = "删除njnz关系")
    @PreAuthorize("@ss.hasPermission('promotion:activityNJNZ:delete')")
    public CommonResult<Boolean> deleteActivityNjnz(@RequestParam(name = "id") Long id) {
        activityNjnzService.deleteActivityNjnz(id);
        return success(true);
    }


    @PreAuthorize("@ss.hasPermission('promotion:activityNJNZ:query')")
    @GetMapping("/selectInfo")
    @Operation(summary = "查询活动详情")
    public CommonResult<ActivityInfoRespVO> selectInfo(@RequestParam("id") Long id) {
        ActivityInfoRespVO activityInfoRespVO = activityNjnzService.selectInfo(id);
        return success(activityInfoRespVO);
    }


    @Operation(summary = "活动数据预览")
    @GetMapping("/get/{id}")
    public CommonResult<ActivityDataAnalysisRespVO> getData(@PathVariable("id") Long id) {
        ActivityDataAnalysisRespVO activityDataAnalysisRespVO = activityNjnzService.getDataAnalysis(id);
        return success(activityDataAnalysisRespVO);
    }


}
