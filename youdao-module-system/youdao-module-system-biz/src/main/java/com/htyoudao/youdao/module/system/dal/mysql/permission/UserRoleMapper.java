package com.htyoudao.youdao.module.system.dal.mysql.permission;

import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.UserRoleDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapperX<UserRoleDO> {

    @DataPermission(enable = false) // 关闭数据权限
    default List<UserRoleDO> selectListByUserId(Long userId, Long businessId) {
        LambdaQueryWrapper<UserRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRoleDO::getUserId, userId);
        if (businessId != null) {
            queryWrapper.eq(UserRoleDO::getBusinessId, businessId);
        }
        return selectList(queryWrapper);
    }

    default List<UserRoleDO> selectListByUserId(Long userId) {
        LambdaQueryWrapper<UserRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRoleDO::getUserId, userId);
        return selectList(queryWrapper);
    }

    default List<UserRoleDO> selectUserListByRoleId(Long roleId) {
        LambdaQueryWrapper<UserRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRoleDO::getRoleId, roleId);
        return selectList(queryWrapper);
    }


    default Integer selectUserCountByRoleId(Long roleId) {
        return selectUserListByRoleId(roleId).size();
    }

    default List<Long> selectBusinessListByUserId(Long userId) {
        LambdaQueryWrapper<UserRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRoleDO::getUserId, userId);
        queryWrapper.select(UserRoleDO::getBusinessId);
        List<UserRoleDO> userRoleDOS = selectList(queryWrapper);
        return userRoleDOS.stream().map(UserRoleDO::getBusinessId).distinct().toList();
    }

    default List<Long> selectUserListByBusinessId(Long businessId) {
        LambdaQueryWrapper<UserRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRoleDO::getBusinessId, businessId);
        queryWrapper.select(UserRoleDO::getUserId);
        List<UserRoleDO> userRoleDOS = selectList(queryWrapper);
        return userRoleDOS.stream().map(UserRoleDO::getUserId).distinct().toList();
    }


    default Integer selectUserCountByBusinessId(Long businessId) {
        return selectUserListByBusinessId(businessId).size();
    }

    default void deleteListByUserIdAndRoleIdIds(Long userId, Collection<Long> roleIds) {
        delete(new LambdaQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO::getUserId, userId)
                .in(UserRoleDO::getRoleId, roleIds));
    }


    default void deleteListByUserIdsAndRoleIdId(List<Long> userIds, Long roleId) {
        delete(new LambdaQueryWrapper<UserRoleDO>()
            .in(UserRoleDO::getUserId, userIds)
            .eq(UserRoleDO::getRoleId, roleId));
    }

    default void deleteListByUserId(Long userId) {
        delete(new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getUserId, userId));
    }

    default void deleteListByRoleId(Long roleId) {
        delete(new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getRoleId, roleId));
    }

    default List<UserRoleDO> selectListByRoleIds(Collection<Long> roleIds) {
        return selectList(UserRoleDO::getRoleId, roleIds);
    }

}
