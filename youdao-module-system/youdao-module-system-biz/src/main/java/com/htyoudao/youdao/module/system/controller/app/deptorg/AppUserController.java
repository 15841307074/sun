package com.htyoudao.youdao.module.system.controller.app.deptorg;

import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.AppDeptUserRespVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptRespVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptUserOAPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptUserOAReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgStoreRespVO;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.*;
import com.htyoudao.youdao.module.system.service.dept.DeptService;
import com.htyoudao.youdao.module.system.service.org.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "app - 老板助手的一些数据")
@RestController
@RequestMapping("/system/user-dept")
@Validated
public class AppUserController {

    @Resource
    private DeptService deptService;

    @Resource
    private OrgService orgService;

    //@PermitAll
    @Operation(summary = "获取全部部门列表")
    @GetMapping("/getAllDeptList")
    @DataPermission(enable = false)
    public CommonResult<List<DeptRespVO>> getAllDeptList() {
        return success(deptService.getAllDeptList());
    }

    @Operation(summary = "获取全部部门+用户列表")
    @GetMapping("/getAllDeptAndUserList")
    @DataPermission(enable = false)
    public CommonResult<List<AppDeptUserRespVO>> getAllDeptAndUserList() {
        return success(deptService.getAllDeptAndUserList());
    }

    //@PermitAll
    @Operation(summary = "获取当前登录人的部门门店信息")
    @GetMapping("/getUserDeptInfo")
    @DataPermission(enable = false)
    public CommonResult<UserDeptInfoRespVO> getUserDeptInfo(@RequestParam(value = "userId") Long userId) {
        return success(deptService.getUserDeptInfo(userId));
    }

    //@PermitAll
    @Operation(summary = "选择人员")
    @GetMapping("/getUserDeptInfoPage")
    @DataPermission(enable = false)
    public CommonResult<PageResult<UserDeptInfoPageRespVO>> getUserDeptInfoPage(UserDeptInfoReqVO userDeptInfoReqVO) {
        return success(deptService.getUserDeptInfoPage(userDeptInfoReqVO));
    }

    //@PermitAll
    @Operation(summary = "获取全部的组织")
    @GetMapping("/getAllOrg")
    @DataPermission(enable = false)
    public CommonResult<List<AppOrgRespVO>> getAllOrg(@RequestParam(value = "businessId") Long businessId) {
        return success(deptService.getAllOrg(businessId));
    }

    //@PermitAll
    @GetMapping("getStoreListByOrgId")
    @Operation(summary = "通过组织查询门店")
    @DataPermission(enable = false)
    public CommonResult<List<AppOrgStoreRespVO>> getStoreListByOrgId(@RequestParam(value = "orgId") Long orgId,
                                                                  @RequestParam(value = "storeName",required = false) String storeName) {
        return success(deptService.getStoreListByOrgId(orgId,storeName));
    }

    //@PermitAll
    @GetMapping("getUserListByDeptId")
    @Operation(summary = "通过部门查询用户")
    @DataPermission(enable = false)
    public CommonResult<List<DeptUserRespVO>> getUserListByDeptId(@RequestParam(value = "deptId") Long deptId,
                                                                  @RequestParam(value = "nameOrMobile",required = false) String nameOrMobile) {
        return success(deptService.getUserListByDeptId(deptId,nameOrMobile));
    }

    //@PermitAll
    @GetMapping("getUserIdsByDept")
    @Operation(summary = "查询当前用户部门下的所有用户")
    //@DataPermission(enable = false)
    public CommonResult<List<Long>> getUserIdsByDept() {
        return success(deptService.getUserIdsByDept());
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

    @GetMapping("getDeptManager")
    @Operation(summary = "选择部门管理人 OA用")
    public CommonResult<DeptUserRespVO> getDeptManager(@RequestParam(value = "deptId",required = false) Long deptId) {
        return success(deptService.getDeptManager(deptId));
    }
}
