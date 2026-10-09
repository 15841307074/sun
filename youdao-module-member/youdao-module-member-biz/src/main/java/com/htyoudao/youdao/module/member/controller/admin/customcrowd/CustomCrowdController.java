package com.htyoudao.youdao.module.member.controller.admin.customcrowd;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo.*;
import com.htyoudao.youdao.module.member.service.crowd.CustomCrowdService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.*;
import java.util.*;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


/**
 * @author dht
 */
@Tag(name = "管理后台 - 自定义人群")
@RestController
@RequestMapping("/member/crowd")
@Validated
public class CustomCrowdController {

    @Resource
    private CustomCrowdService crowdService;

    @PostMapping("/page")
    @Operation(summary = "自定义人群分页")
    //@PreAuthorize("@ss.hasPermission('member:crowd:page')")
    public CommonResult<PageResult<CustomCrowdPageRespVO>> page(@Valid @RequestBody CustomCrowdPageReqVO reqVO) {
        return success(crowdService.page(reqVO));
    }


    @GetMapping("/dropDown")
    @Operation(summary = "自定义人群下拉")
    //@PreAuthorize("@ss.hasPermission('member:crowd:page')")
    public CommonResult<List<CustomCrowdDropDownRespVO>> dropDown() {
        return success(crowdService.dropDown());
    }

    @PostMapping("/getById")
    @Operation(summary = "详情")
    //@PreAuthorize("@ss.hasPermission('member:crowd:getById')")
    public CommonResult<CustomCrowdRespVO> getById(@RequestParam("id") Long id) {
        return success(crowdService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "创建自定义人群")
    //@PreAuthorize("@ss.hasPermission('member:crowd:create')")
    public CommonResult<Long> createCrowd(@Valid @RequestBody CustomCrowdSaveReqVO createReqVO) {
        return success(crowdService.createCrowd(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新自定义人群")
    //@PreAuthorize("@ss.hasPermission('member:crowd:update')")
    public CommonResult<Boolean> updateCrowd(@Valid @RequestBody CustomCrowdSaveReqVO updateReqVO) {
        crowdService.updateCrowd(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除自定义人群")
    @Parameter(name = "id", description = "编号", required = true)
    //@PreAuthorize("@ss.hasPermission('member:crowd:delete')")
    public CommonResult<Boolean> deleteCrowd(@RequestParam("id") Long id) {
        crowdService.deleteCrowd(id);
        return success(true);
    }
}