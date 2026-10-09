package com.htyoudao.youdao.module.system.controller.app.complaint;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintDetailRespVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintRespVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintUpdateReqVO;
import com.htyoudao.youdao.module.system.controller.app.complaint.vo.SysComplaintSaveVO;
import com.htyoudao.youdao.module.system.service.complant.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

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
public class AppComplaintController {

    @Resource
    private ComplaintService complaintService;

    @GetMapping("/page")
    @Operation(summary = "投诉分页列表--app")
    @PermitAll
    public CommonResult<PageResult<ComplaintRespVO>> getComplaintListPage(@Valid ComplaintPageReqVO pageReqVO) {
        // 获得用户分页列表
        PageResult<ComplaintRespVO> pageResult = complaintService.getComplaintListPageApp(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(pageResult);
    }
    @GetMapping("/get")
    @Operation(summary = "获得投诉详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<ComplaintDetailRespVO> getComplaint(@RequestParam("id") Long id) {
        ComplaintDetailRespVO complaintDetailRespVO = complaintService.getComplaint(id);
        return success(complaintDetailRespVO);
    }
    @PutMapping("/update")
    @Operation(summary = "处理投诉")
    @PermitAll
    public CommonResult<Boolean> updateComplaint(@Valid @RequestBody ComplaintUpdateReqVO reqVO) {
        complaintService.updateComplaint(reqVO);
        return success(true);
    }
    @GetMapping("/getComplaintCount")
    @Operation(summary = "待处理数量")
    @PermitAll
    public CommonResult<Long> getComplaintCount() throws IOException {
        return success(complaintService.getComplaintCount());
    }
    /**
     * 小程序
     */
    @PostMapping("/addComplaint")
    @Operation(summary = "投诉新增")
    @PermitAll
    public CommonResult addComplaint(@RequestBody SysComplaintSaveVO sysComplaintVo) {
        log.info("投诉新增---------");
        complaintService.addComplaint(sysComplaintVo);
        return success(true);
    }
    @GetMapping("/pageByStore")
    @Operation(summary = "投诉分页列表--老板助手")
    @PermitAll
    public CommonResult<PageResult<ComplaintRespVO>> getComplaintListPageByStore(@Valid ComplaintPageReqVO pageReqVO) {
        // 获得用户分页列表
        pageReqVO.setIsBoss(Boolean.TRUE);
        PageResult<ComplaintRespVO> pageResult = complaintService.getComplaintListPageApp(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(pageResult);
    }
    @GetMapping("/pageByStoreSum")
    @Operation(summary = "投诉分页列表数量统计--老板助手")
    @PermitAll
    public CommonResult<Map<Integer, Long>> pageByStoreSum(@Valid ComplaintPageReqVO pageReqVO) {
        pageReqVO.setIsBoss(Boolean.TRUE);
        Map<Integer, Long> map = complaintService.pageByStoreSum(pageReqVO);
        return success(map);
    }
}
