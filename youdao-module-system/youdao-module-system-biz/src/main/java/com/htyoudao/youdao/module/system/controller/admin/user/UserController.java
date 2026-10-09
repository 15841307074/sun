package com.htyoudao.youdao.module.system.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.excel.core.util.ExcelUtils;
import com.htyoudao.youdao.module.system.controller.admin.business.vo.BusinessSimpleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.*;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.service.business.BusinessService;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.FILE_IS_EMPTY;

@Tag(name = "管理后台 - 用户")
@RestController
@RequestMapping("/system/user")
@Validated
public class UserController {

    @Resource
    private AdminUserService userService;
    @Resource
    private BusinessService businessService;

    @PostMapping("/create")
    @Operation(summary = "新增用户")
    @PreAuthorize("@ss.hasPermission('system:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody UserSaveReqVO reqVO) {
        Long id = userService.createUser(reqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "修改用户")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody UserSaveReqVO reqVO) {
        userService.updateUser(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return success(true);
    }

    @DeleteMapping("/deletes")
    @Operation(summary = "批量删除用户")
    @Parameter(name = "ids", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:user:deletes')")
    public CommonResult<Boolean> deletesUser(@RequestParam("ids") List<Long> ids) {
        userService.deleteUsers(ids);
        return success(true);
    }

    @DeleteMapping("/deleteDeactivated")
    @Operation(summary = "删除停用用户")
    @PreAuthorize("@ss.hasPermission('system:user:deleteDeactivated')")
    public CommonResult<Boolean> deleteDeactivated() {
        userService.deleteDeactivated();
        return success(true);
    }

    @PutMapping("/update-password")
    @Operation(summary = "重置用户密码")
    @PreAuthorize("@ss.hasPermission('system:user:update-password')")
    public CommonResult<Boolean> updateUserPassword(@Valid @RequestBody UserUpdatePasswordReqVO reqVO) {
        userService.updateUserPassword(reqVO.getId(), reqVO.getPassword());
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改用户状态")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> updateUserStatus(@Valid @RequestBody UserUpdateStatusReqVO reqVO) {
        userService.updateUserStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @PostMapping("/page")
    @Operation(summary = "获得用户分页列表")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@RequestBody UserPageReqVO pageReqVO) {
        // 获得用户分页列表
        PageResult<UserRespVO> pageResult = userService.getUserPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        // 拼接数据
        for (UserRespVO user : pageResult.getList()) {
            BusinessSimpleRespVO businessDo = businessService.getBusinessRoleByUserId(user.getId());
            if (businessDo != null) {
                if (!businessDo.getRoleList().isEmpty()) {
                    user.setRoleName(businessDo.getRoleList().stream()
                            .map(RoleSimpleRespVO::getName)
                            .collect(Collectors.joining(",")));
                }
                user.setStoreCount(businessDo != null && businessDo.getStoreCount() != null ? businessDo.getStoreCount() : 0);
                user.setOrgName(businessDo.getOrgName());
                user.setDeptId(businessDo.getDeptId());
                user.setDeptName(businessDo.getDeptName());
                user.setDeptUserType(businessDo.getDeptUserType());
                user.setStoreList(businessDo.getStoreList());
                user.setUserDeptId(businessDo.getUserDeptId());
            }
        }
        Comparator<UserRespVO> comparator = Comparator
                // 明确泛型<UserRespVO, Boolean>，指定user为UserRespVO类型
                .comparing((UserRespVO user) -> user.getDeptUserType() != null && user.getDeptUserType() == 1,
                        Comparator.reverseOrder())
                // 同样补充泛型或显式指定参数类型
                .thenComparing((UserRespVO user) -> user.getIsProject() != null && user.getIsProject() == 1,
                        Comparator.reverseOrder())
                .thenComparing(UserRespVO::getDeptUserType, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(UserRespVO::getIsProject, Comparator.nullsLast(Comparator.naturalOrder()));
        pageResult.getList().sort(comparator);
        return success(pageResult);
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获取用户精简信息列表", description = "只包含被开启的用户，主要用于前端的下拉选项")
    public CommonResult<List<UserSimpleVO>> getSimpleUserList(@RequestParam("userName") String userName) {
        List<UserSimpleVO> list = userService.getUserListByStatus(CommonStatusEnum.ENABLE.getStatus(), userName);
        return success(list);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<UserRespVO> getUser(@RequestParam("id") Long id) {
        UserRespVO user = userService.getUserDetail(id);
        if (user == null) {
            return success(null);
        }
        // 拼接数据

        BusinessSimpleRespVO businessDo = businessService.getBusinessRoleByUserId(id);
        if (businessDo != null) {
            if (!businessDo.getRoleList().isEmpty()) {
                user.setRoleName(businessDo.getRoleList().stream()
                        .map(RoleSimpleRespVO::getName)
                        .collect(Collectors.joining(",")));
            }
            user.setStoreCount(businessDo.getStoreCount());
            user.setOrgName(businessDo.getOrgName());
            user.setRoleList(businessDo.getRoleList());
        }
        List<BusinessSimpleRespVO> list = businessService.getBusinessListRoleByUserId(id);
        user.setBusinessList(list);
        return success(user);
    }

    @GetMapping("/getMyInfo")
    @Operation(summary = "获得用户详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<UserRespVO> getMyInfo(@RequestParam("id") Long id) {
        UserRespVO user = userService.getUserDetail(id);
        if (user == null) {
            return success(null);
        }
        // 拼接数据

        BusinessSimpleRespVO businessDo = businessService.getBusinessRoleByUserId(id);
        if (businessDo != null) {
            if (!businessDo.getRoleList().isEmpty()) {
                user.setRoleName(businessDo.getRoleList().stream()
                        .map(RoleSimpleRespVO::getName)
                        .collect(Collectors.joining(",")));
            }
            user.setStoreCount(businessDo.getStoreCount());
            user.setOrgName(businessDo.getOrgName());
            user.setStoreList(businessDo.getStoreList());
            user.setRoleList(businessDo.getRoleList());
        }
        List<BusinessSimpleRespVO> list = businessService.getBusinessListRoleByUserId(id);
        user.setBusinessList(list);
        return success(user);
    }

    @PostMapping("/export")
    @Operation(summary = "导出用户")
    @PreAuthorize("@ss.hasPermission('system:user:export')")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportUserList(@RequestBody UserPageReqVO exportReqVO, HttpServletRequest
            request, HttpServletResponse response) throws IOException {
        userService.exportUserList(exportReqVO, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入用户模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<UserImportExcelVO> list = Arrays.asList(
                UserImportExcelVO.builder().username("0090").mobile("15601691300")
                        .nickname("youdao").status(CommonStatusEnum.ENABLE.getStatus()).build(),
                UserImportExcelVO.builder().username("yuanma").mobile("15601701300")
                        .nickname("源码").status(CommonStatusEnum.DISABLE.getStatus()).build()
        );
        // 输出
        ExcelUtils.write(response, "用户导入模板.xls", "用户列表", UserImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入用户")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支  持更新，默认为 false", example = "true")
    })
//    @PreAuthorize("@ss.hasPermission('system:user:import')")
    public void importExcel(@RequestParam("file") MultipartFile file) throws Exception {
//        List<UserImportExcelVO> list = ExcelUtils.read(file, UserImportExcelVO.class);
//        return success(userService.importUserList(list, updateSupport));
        userService.importUserList(file);
    }

    @GetMapping("/getCheckPhone")
    @Operation(summary = "校验用户是否存在")
    @Parameter(name = "mobile", description = "手机号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<UserRespVO> getCheckPhone(@RequestParam("mobile") String mobile) {
        return success(userService.checkMobile(mobile));
    }

    @GetMapping("/getCheckPhoneByBusiness")
    @Operation(summary = "校验用户是否存在--创建项目")
    @Parameter(name = "mobile", description = "手机号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<UserRespVO> getCheckPhoneByBusiness(@RequestParam("mobile") String mobile) {
        return success(userService.getCheckPhoneByBusiness(mobile));
    }

    @GetMapping("/getMyDetail")
    @Operation(summary = "获得我的详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<UserRespVO> getMyDetail(@RequestParam("id") Long id) {
        UserRespVO user = userService.getUserDetail(id);
        if (user == null) {
            return success(null);
        }
        // 拼接数据
        BusinessSimpleRespVO businessDo = businessService.getBusinessRoleByUserId(id);
        if (businessDo != null) {
            if (!businessDo.getRoleList().isEmpty()) {
                user.setRoleName(businessDo.getRoleList().stream()
                        .map(RoleSimpleRespVO::getName)
                        .collect(Collectors.joining(",")));
            }
            user.setStoreCount(businessDo.getStoreCount());
            user.setOrgName(businessDo.getOrgName());
            user.setRoleList(businessDo.getRoleList());
        }
        return success(user);
    }

    @GetMapping("/getRegionalManagerUserList")
    @Operation(summary = "获取区域经理列表")
    public CommonResult<List<AdminUserDO>> getRegionalManagerUserList() {
        return success(userService.getRegionalManagerUserList());
    }


    /**
     * 批量发放优惠卷
     */
    @PostMapping(value = "/importUserDeptV2")
    @Operation(summary = "导入OA system_user + business_user + dept_user")
    @DataPermission(enable = false)
    public CommonResult<Boolean> importCouponExl(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw exception(FILE_IS_EMPTY);
        }
        return success(userService.importUserDeptV2(file));
    }
}
