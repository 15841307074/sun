package com.htyoudao.youdao.module.system.controller.admin.dept;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptDTO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.*;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.DeptUserRespVO;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.UserDeptInfoRespVO;
import com.htyoudao.youdao.module.system.service.dept.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "管理后台 - 部门")
@RestController
@RequestMapping("/system/dept")
@Validated
public class DeptController {

    @Resource
    private DeptService deptService;

    @Operation(summary = "获取部门列表")
    @PreAuthorize("@ss.hasPermission('system:dept:list')")
    @GetMapping("/list")
    @DataPermission(enable = false)
    public CommonResult<List<DeptRespVO>> getDeptList() {
        return success(deptService.getDeptList());
    }

    @Operation(summary = "人员点移动按钮的时候弹出的部门树")
    @PreAuthorize("@ss.hasPermission('system:dept:list')")
    @GetMapping("/getDeptListByLoginUser")
    public CommonResult<List<DeptRespVO>> getDeptListByLoginUser() {
        return success(deptService.getDeptListByLoginUser());
    }


    @PostMapping("create")
    @Operation(summary = "创建部门")
    @PreAuthorize("@ss.hasPermission('system:dept:create')")
    @DataPermission(enable = false)
    public CommonResult<Long> createDept(@Valid @RequestBody DeptSaveReqVO createReqVO) {
        Long deptId = deptService.createDept(createReqVO);
        return success(deptId);
    }

    @PutMapping("update")
    @Operation(summary = "更新部门")
    @PreAuthorize("@ss.hasPermission('system:dept:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateDept(@Valid @RequestBody DeptSaveReqVO updateReqVO) {
        deptService.updateDept(updateReqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @Operation(summary = "删除部门")
    @Parameter(name = "id", description = "id", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dept:delete')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> deleteDept(@RequestParam("id") Long id) {
        deptService.deleteDept(id);
        return success(true);
    }

    @Operation(summary = "部门添加用户的弹窗列表")
    @PreAuthorize("@ss.hasPermission('system:dept:deptSaveUser')")
    @GetMapping("/getUserListWithDept")
    @DataPermission(enable = false)
    public CommonResult<PageResult<DeptUserPageRespVO>> getUserListWithDept(DeptUserPageReqVO reqVO) {
        return success(deptService.getUserListWithDept(reqVO));
    }

    @PostMapping("deptSaveUser")
    @Operation(summary = "部门添加成员")
    @PreAuthorize("@ss.hasPermission('system:dept:deptSaveUser')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> deptSaveUser(@Valid @RequestBody DeptSaveUserReqVO createReqVO) {
        return success(deptService.deptSaveUser(createReqVO));
    }


    @PostMapping("moveDeptUser")
    @Operation(summary = "部门成员移动")
    @PreAuthorize("@ss.hasPermission('system:dept:moveDeptUser')")
    @DataPermission(enable = false)
    public CommonResult<Integer> moveDeptUser(@Valid @RequestBody MoveDeptUserReqVO moveReqVO) {
        return success(deptService.moveDeptUser(moveReqVO));
    }

    @PostMapping("removeDeptUser")
    @Operation(summary = "部门成员移除")
    @PreAuthorize("@ss.hasPermission('system:dept:removeDeptUser')")
    @DataPermission(enable = false)
    public CommonResult<Integer> removeDeptUser(@Valid @RequestBody RemoveDeptUserReqVO removeReqVO) {
        return success(deptService.removeDeptUser(removeReqVO));
    }


    @PutMapping("deptUserSetCharge")
    @Operation(summary = "设置为负责人")
    @PreAuthorize("@ss.hasPermission('system:dept:deptUserSetCharge')")
    @DataPermission(enable = false)
    public CommonResult<Integer> deptUserSetCharge(@Valid @RequestBody DeptUserSetChargeReqVO chargeReqVO) {
        return success(deptService.deptUserSetCharge(chargeReqVO));
    }

    @PutMapping("deptUserUnCharge")
    @Operation(summary = "撤销负责人")
    @PreAuthorize("@ss.hasPermission('system:dept:deptUserSetCharge')")
    @DataPermission(enable = false)
    public CommonResult<Integer> deptUserUnCharge(@Valid @RequestBody DeptUserSetChargeReqVO chargeReqVO) {
        return success(deptService.deptUserUnCharge(chargeReqVO));
    }


    @PostMapping("getUserAndDeptPageWithOA")
    @Operation(summary = "获取用户信息 OA用")
    @DataPermission(enable = false)
    public CommonResult<PageResult<DeptUserOAPageRespVO>> getUserAndDeptPageWithOA(@Valid @RequestBody DeptUserOAReqVO deptUserOAReqVO) {
        return success(deptService.getUserAndDeptPageWithOA(deptUserOAReqVO));
    }


    @PostMapping("getAllUserAndDeptPageWithOA")
    @Operation(summary = "选择所有用户信息 OA用")
    @DataPermission(enable = false)
    public CommonResult<List<DeptUserOAPageRespVO>> getAllUserAndDeptPageWithOA(@Valid @RequestBody DeptUserOAReqVO deptUserOAReqVO) {
        return success(deptService.getAllUserAndDeptPageWithOA(deptUserOAReqVO));
    }

    @Operation(summary = "获取全部部门列表")
    @GetMapping("/getAllDeptList")
    @DataPermission(enable = false)
    public CommonResult<List<DeptRespVO>> getAllDeptList() {
        return success(deptService.getAllDeptList());
    }

    @Operation(summary = "获取当前登录人的部门门店信息")
    @GetMapping("/getUserDeptInfo")
    @DataPermission(enable = false)
    public CommonResult<UserDeptInfoRespVO> getUserDeptInfo(@RequestParam(value = "userId") Long userId) {
        return success(deptService.getUserDeptInfo(userId));
    }

    @Operation(summary = "获取部门负责人")
    @GetMapping("/getDeptManager")
    @DataPermission(enable = false)
    public CommonResult<DeptUserRespVO> getDeptManager(@RequestParam(value = "deptId") Long deptId) {
        return success(deptService.getDeptManager(deptId));
    }

//    @Operation(summary = "getParentDeptList")
//    @GetMapping("/getParentDeptList")
//    @DataPermission(enable = false)
//    public CommonResult<Map<Long, DeptDTO>> getParentDeptList(@RequestParam(value = "ids") List<Long > ids) {
//        return success(deptService.getParentDeptList(ids));
//    }
//
//    @Operation(summary = "getSonDeptList")
//    @GetMapping("/getSonDeptList")
//    @DataPermission(enable = false)
//    public CommonResult<List<Long>> getSonDeptList(@RequestParam(value = "deptId") Long deptId) {
//        return success(deptService.getSonDeptList(deptId));
//    }

}
