package com.htyoudao.youdao.module.system.dal.mysql.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.UserPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.UserRespVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.UserSimpleVO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.enums.UserTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper
public interface AdminUserMapper extends BaseMapperX<AdminUserDO> {

    default AdminUserDO selectByUsername(String username) {
        return selectOne(AdminUserDO::getUsername, username);
    }

    default AdminUserDO selectByEmail(String email) {
        return selectOne(AdminUserDO::getEmail, email);
    }

    default AdminUserDO selectByMobile(String mobile) {
        return selectOne(
                new LambdaQueryWrapperX<AdminUserDO>()
                        .eq(AdminUserDO::getMobile, mobile)
//                        .eq(AdminUserDO::getUserType, UserTypeEnum.T_00.getStatus())
        );
    }

    default AdminUserDO selectByMobileWithGyl(String mobile) {
        return selectOne(
                new LambdaQueryWrapperX<AdminUserDO>()
                        .eq(AdminUserDO::getMobile, mobile)
                        .ne(AdminUserDO::getUserType, UserTypeEnum.T_00.getStatus())
        );
    }

    default PageResult<AdminUserDO> selectPage(UserPageReqVO reqVO, Collection<Long> deptIds) {
        LambdaQueryWrapperX<AdminUserDO> wrapper = new LambdaQueryWrapperX<>();
        if (reqVO.getText() != null && !reqVO.getText().isEmpty()) {
            wrapper.and(w -> w
                    .like(AdminUserDO::getUsername, reqVO.getText())
                    .or()
                    .like(AdminUserDO::getMobile, reqVO.getText())
                    .or()
                    .like(AdminUserDO::getNickname, reqVO.getText()));
        }
        wrapper.eqIfPresent(AdminUserDO::getStatus, reqVO.getStatus())
                .orderByDesc(AdminUserDO::getCreateTime);

        return selectPage(reqVO, wrapper);
    }

    default List<AdminUserDO> selectListByNickname(String nickname) {
        return selectList(new LambdaQueryWrapperX<AdminUserDO>().like(AdminUserDO::getNickname, nickname));
    }

    default List<AdminUserDO> selectListByStatus(Integer status) {
        return selectList(AdminUserDO::getStatus, status);
    }

    default List<AdminUserDO> selectListByDeptIds(Collection<Long> deptIds) {
        return selectList(AdminUserDO::getDeptId, deptIds);
    }

    IPage<UserRespVO> selectPageList(Page<UserRespVO> page, @Param("record") UserPageReqVO reqVO, @Param("orgIds") Set<Long> orgIds,Long businessId);

    /**
     * 查询不在组织里的用户
     * @param page page
     * @param pageReqVO pageReqVO
     * @return Page
     */
    Page<AdminUserDO> getUserListNotInOrg(@Param("page") Page<AdminUserDO> page,@Param("pageReqVO") OrgUserPageReqVO pageReqVO);
    // 下拉列表
    List<UserSimpleVO> selectUserList(@Param("businessId") Long businessId,@Param("userName") String userName);

    /**
     * 分页查询用户列表 无组织/某组织
     * @param page page
     * @param queryWrapper queryWrapper
     * @return AdminUserDO
     */
    Page<AdminUserDO> getUserListWithOrgOrStore(@Param("page") Page<AdminUserDO> page,@Param("ew") QueryWrapper<AdminUserDO> queryWrapper);

    /**
     * 根据部门id查询用户列表
     * @param deptId deptId
     * @param nameOrMobile nameOrMobile
     * @return List<AdminUserDO>
     */
    List<AdminUserDO> getUserListByDeptId(Long deptId, String nameOrMobile);
}
