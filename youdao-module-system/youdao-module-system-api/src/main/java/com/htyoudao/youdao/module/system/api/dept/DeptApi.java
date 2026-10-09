package com.htyoudao.youdao.module.system.api.dept;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptDTO;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import com.htyoudao.youdao.module.system.api.dept.dto.UserDeptInfoRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashSet;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "RPC 服务 - 部门")
public interface DeptApi {


    @Operation(summary = "获得部门信息")
    @Parameter(name = "id", description = "部门编号", example = "1024", required = true)
    CommonResult<DeptRespDTO> getDept(@RequestParam("id") Long id);

    @Operation(summary = "获得部门信息数组")
    @Parameter(name = "ids", description = "部门编号数组", example = "1,2", required = true)
    CommonResult<Map<Long,DeptRespDTO>> getDeptListByUserIds(@RequestParam("ids") Collection<Long> ids);

    @Operation(summary = "校验部门是否合法")
    @Parameter(name = "ids", description = "部门编号数组", example = "1,2", required = true)
    CommonResult<Boolean> validateDeptList(@RequestParam("ids") Collection<Long> ids);

    /**
     * 获得指定编号的部门 Map
     *
     * @param ids 部门编号数组
     * @return 部门 Map
     */
    default Map<Long, DeptRespDTO> getDeptMap(Collection<Long> ids) {
        List<DeptRespDTO> list = getDeptList(new HashSet<>(ids)).getCheckedData();
        return CollectionUtils.convertMap(list, DeptRespDTO::getId);
    }

    @Operation(summary = "获得指定部门的所有子部门")
    @Parameter(name = "id", description = "部门编号", example = "1024", required = true)
    CommonResult<List<DeptRespDTO>> getChildDeptList(@RequestParam("id") Long id);

    CommonResult<List<DeptRespDTO>> getDeptList(Set<Long> deptIds);

    CommonResult<Long> getDeptManager(Long deptId);

    CommonResult<List<Long>> getManagerDeptByUserId(Long userId);

    CommonResult<UserDeptInfoRespDTO> getUserDeptInfo(Long userId);

    /**
     * 获取父部门集合
     * @param ids ids
     * @return Map<Long,DeptDTO>
     */
    CommonResult<Map<Long, DeptDTO>> getParentDeptList(List<Long > ids);

    /**
     * 获取子部门集合
     * @param id id
     * @return Map<Long,List<DeptDTO>>
     */
    CommonResult<List<Long>> getSonDeptList(Long id);

}
