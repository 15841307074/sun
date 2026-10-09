package com.htyoudao.youdao.module.system.api.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.UserDeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.dept.UserDeptMapper;
import com.htyoudao.youdao.module.system.service.dept.DeptService;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import java.util.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;

@DubboService
@Validated
public class AdminUserApiImpl implements AdminUserApi {

    private static final Logger log = LoggerFactory.getLogger(AdminUserApiImpl.class);
    @Resource
    private AdminUserService userService;
    @Resource
    private DeptService deptService;

    @Resource
    private UserDeptMapper userDeptMapper;

    @Override
    public CommonResult<AdminUserRespDTO> getUser(Long id) {
        AdminUserDO user = userService.getUser(id);
        //只支持放一个 只能写死10
        UserDeptDO userDeptDO = userDeptMapper.selectOne(new LambdaQueryWrapper<UserDeptDO>().eq(UserDeptDO::getUserId, id).eq(UserDeptDO::getBusinessId, 10));
        AdminUserRespDTO bean = BeanUtils.toBean(user, AdminUserRespDTO.class);
        if(ObjectUtil.isNotEmpty(userDeptDO)){
            bean.setDeptId(userDeptDO.getDeptId());
        }
        return success(bean);
    }

//    @Override
//    public CommonResult<List<AdminUserRespDTO>> getUserListBySubordinate(Long id) {
//        // 1.1 获取用户负责的部门
//        AdminUserDO user = userService.getUser(id);
//        if (user == null) {
//            return success(Collections.emptyList());
//        }
//        ArrayList<Long> deptIds = new ArrayList<>();
//        DeptDO dept = deptService.getDept(user.getDeptId());
//        if (dept == null) {
//            return success(Collections.emptyList());
//        }
//        if (ObjUtil.notEqual(dept.getLeaderUserId(), id)) { // 校验为负责人
//            return success(Collections.emptyList());
//        }
//        deptIds.add(dept.getId());
//        // 1.2 获取所有子部门
//        List<DeptDO> childDeptList = deptService.getChildDeptList(dept.getId());
//        if (CollUtil.isNotEmpty(childDeptList)) {
//            deptIds.addAll(convertSet(childDeptList, DeptDO::getId));
//        }
//
//        // 2. 获取部门对应的用户信息
//        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
//        users.removeIf(item -> ObjUtil.equal(item.getId(), id)); // 排除自己
//        return success(BeanUtils.toBean(users, AdminUserRespDTO.class));
//    }

    @Override
    @DataPermission(enable = false) // 禁用数据权限。原因是，一般基于指定 id 的 API 查询，都是数据拼接为主
    public CommonResult<List<AdminUserRespDTO>> getUserList(Collection<Long> ids) {
        List<AdminUserDO> users = userService.getUserList(ids);
        log.info("getUserList: {}", users);
        Map<Long, DeptDO> userDeptMap = deptService.getDeptListByUserIds(ids);
        List<AdminUserRespDTO> bean = BeanUtils.toBean(users, AdminUserRespDTO.class);
        log.info("userDeptMap: {}", userDeptMap);
        if(ObjectUtil.isEmpty(userDeptMap)){
            return success(bean);
        }
        for (AdminUserRespDTO adminUserRespDTO : bean) {
            DeptDO deptDO = userDeptMap.get(adminUserRespDTO.getId());
            adminUserRespDTO.setDeptId(ObjectUtil.isNotEmpty(deptDO) ? deptDO.getId() : null);
            adminUserRespDTO.setDeptName(ObjectUtil.isNotEmpty(deptDO) ? deptDO.getName() : null);
        }
        return success(bean);
    }

    @Override
    public CommonResult<List<AdminUserRespDTO>> getUserListByDeptIds(Collection<Long> deptIds) {
        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
        return success(BeanUtils.toBean(users, AdminUserRespDTO.class));
    }


    @Override
    public CommonResult<Boolean> validateUserList(Collection<Long> ids) {
        userService.validateUserList(ids);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> isStoreLeader(Long userId) {
        return userService.isStoreLeader(userId);
    }
}
