package com.htyoudao.youdao.module.system.dal.mysql.permission;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.UserRoleVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper
public interface RoleMapper extends BaseMapperX<RoleDO> {

    default PageResult<RoleDO> selectPage(RolePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RoleDO>()
                .likeIfPresent(RoleDO::getName, reqVO.getName())
                .likeIfPresent(RoleDO::getCode, reqVO.getCode())
                .eqIfPresent(RoleDO::getStatus, reqVO.getStatus())
                .orderByAsc(RoleDO::getSort));
    }

    default Integer getMaxSort(){
        RoleDO roleDO = selectOne(new LambdaQueryWrapperX<RoleDO>()
            .select(RoleDO::getSort)
            .orderByDesc(RoleDO::getSort)
            .last("limit 1"));

        if (roleDO == null){
            return 0;
        }
        return roleDO.getSort();
    }

    default RoleDO selectByName(String name) {
        return selectOne(RoleDO::getName, name);
    }

    default RoleDO selectByCode(String code) {
        return selectOne(RoleDO::getCode, code);
    }

    default List<RoleDO> selectListByStatus(@Nullable Collection<Integer> statuses) {
        return selectList(RoleDO::getStatus, statuses);
    }

    @DataPermission(enable = false) //
    default List<RoleDO> selectListByBusiness(@Nullable Long BusinessId) {
        return selectList(RoleDO::getBusinessId, BusinessId);
    }

    /**
     * 根据用户id查询角色信息
     * @param userIds userIds
     * @return List<UserRoleVO>
     */
    List<UserRoleVO> getRoleNamesByUserIds(@Param("userIds") Set<Long> userIds);

    /**
     * 根据用户id查询角色信息
     * @param userIds userIds
     * @return List<UserRoleVO>
     */
    List<UserRoleVO> getRoleNamesByUserIdsV2(Set<Long> userIds, Long loginBusinessId);
}
