package com.htyoudao.youdao.module.system.api.user;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Tag(name = "RPC 服务 - 管理员用户")
public interface AdminUserApi {

    @Operation(summary = "通过用户 ID 查询用户")
    @Parameter(name = "id", description = "用户编号", example = "1", required = true)
    CommonResult<AdminUserRespDTO> getUser(@RequestParam("id") Long id);

//    @Operation(summary = "通过用户 ID 查询用户下属")
//    @Parameter(name = "id", description = "用户编号", example = "1", required = true)
//    CommonResult<List<AdminUserRespDTO>> getUserListBySubordinate(@RequestParam("id") Long id);

    @Operation(summary = "通过用户 ID 查询用户们")
    @Parameter(name = "ids", description = "部门编号数组", example = "1,2", required = true)
    CommonResult<List<AdminUserRespDTO>> getUserList(@RequestParam("ids") Collection<Long> ids);

    @Operation(summary = "获得指定部门的用户数组")
    @Parameter(name = "deptIds", description = "部门编号数组", example = "1,2", required = true)
    CommonResult<List<AdminUserRespDTO>> getUserListByDeptIds(@RequestParam("deptIds") Collection<Long> deptIds);


    /**
     * 获得用户 Map
     *
     * @param ids 用户编号数组
     * @return 用户 Map
     */
    default Map<Long, AdminUserRespDTO> getUserMap(Collection<Long> ids) {
        List<AdminUserRespDTO> users = getUserList(ids).getCheckedData();
        return CollectionUtils.convertMap(users, AdminUserRespDTO::getId);
    }

    /**
     * 校验用户是否有效。如下情况，视为无效：
     * 1. 用户编号不存在
     * 2. 用户被禁用
     *
     * @param id 用户编号
     */
    default void validateUser(Long id) {
        validateUserList(Collections.singleton(id));
    }

    @Operation(summary = "校验用户们是否有效")
    @Parameter(name = "ids", description = "用户编号数组", example = "3,5", required = true)
    CommonResult<Boolean> validateUserList(@RequestParam("ids") Collection<Long> ids);

    /**
     * 校验用户是否是门店店长
     * @param userId userId
     * @return Boolean
     */
    CommonResult<Boolean> isStoreLeader(Long userId);
}
