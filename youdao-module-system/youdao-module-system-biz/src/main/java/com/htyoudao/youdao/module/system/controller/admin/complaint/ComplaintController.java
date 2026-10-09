package com.htyoudao.youdao.module.system.controller.admin.complaint;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.business.vo.BusinessSimpleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintDetailRespVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintRespVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintUpdateReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.UserRespVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.UserUpdateStatusReqVO;
import com.htyoudao.youdao.module.system.service.complant.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


/**
 * <p>
 * 投诉建议模块
 * </p>
 *
 * @author dingyunfei
 * @since 2024-05-12
 */
@RestController
@Slf4j
@Tag(name = "投诉建议模块", description = "投诉建议模块")
@RequestMapping("/system/complaint")
@Validated
public class ComplaintController {

    @Resource
    private ComplaintService complaintService;

    @GetMapping("/page")
    @Operation(summary = "投诉分页列表")
    @PreAuthorize("@ss.hasPermission('system:complaint:query')")
    public CommonResult<PageResult<ComplaintRespVO>> getComplaintListPage(@Valid ComplaintPageReqVO pageReqVO) {
        // 获得用户分页列表
        PageResult<ComplaintRespVO> pageResult = complaintService.getComplaintListPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(pageResult);
    }
    @GetMapping("/get")
    @Operation(summary = "获得投诉详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:complaint:query')")
    public CommonResult<ComplaintDetailRespVO> getComplaint(@RequestParam("id") Long id) {
        ComplaintDetailRespVO complaintDetailRespVO = complaintService.getComplaint(id);
        return success(complaintDetailRespVO);
    }
    @PutMapping("/update")
    @Operation(summary = "处理投诉")
    @PreAuthorize("@ss.hasPermission('system:complaint:update')")
    public CommonResult<Boolean> updateComplaint(@Valid @RequestBody ComplaintUpdateReqVO reqVO) {
        complaintService.updateComplaint(reqVO);
        return success(true);
    }
    @GetMapping("/getComplaintCount")
    @Operation(summary = "待处理数量")
    public CommonResult<Long> getComplaintCount() throws IOException {
        return success(complaintService.getComplaintCount());
    }
}
