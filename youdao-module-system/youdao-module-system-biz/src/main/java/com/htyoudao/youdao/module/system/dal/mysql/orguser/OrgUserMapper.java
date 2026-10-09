package com.htyoudao.youdao.module.system.dal.mysql.orguser;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserMoveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserNum;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.orguser.OrgUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.UserRoleDO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.module.system.controller.admin.orguser.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 组织和用户关联 Mapper
 *
 * @author 零零玖零
 */
@Mapper
public interface OrgUserMapper extends BaseMapperX<OrgUserDO> {

    /**
     * 默认分页
     * @param reqVO reqVO
     * @return OrgUserDO
     */
    default PageResult<OrgUserDO> selectPage(OrgUserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OrgUserDO>()
                .eqIfPresent(OrgUserDO::getUserId, reqVO.getUserId())
                .eqIfPresent(OrgUserDO::getOrgId, reqVO.getOrgId())
                .betweenIfPresent(OrgUserDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(OrgUserDO::getType, reqVO.getType())
                .eqIfPresent(OrgUserDO::getBusinessId, reqVO.getBusinessId())
                .orderByDesc(OrgUserDO::getId));
    }

    /**
     * 负责人在最前分页
     * @param reqVO reqVO
     * @return OrgUserDO
     */
    default PageResult<OrgUserDO> selectPageOrderByType(OrgUserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OrgUserDO>()
                .eqIfPresent(OrgUserDO::getUserId, reqVO.getUserId())
                .eqIfPresent(OrgUserDO::getOrgId, reqVO.getOrgId())
                .betweenIfPresent(OrgUserDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(OrgUserDO::getType, reqVO.getType())
                .eqIfPresent(OrgUserDO::getBusinessId, reqVO.getBusinessId())
                .orderByDesc(OrgUserDO::getType, OrgUserDO::getCreateTime));
    }

    /**
     * 根据用户id查询用户组织id集合
     * @param userId userId
     * @return List<Long>
     */
    List<Long> selectUserOrgIds(Long userId);

    /**
     * 根据用户id查询用户本级与下级所有组织id集合
     * @param userId userId
     * @return List<Long>
     */
    List<Long> selectAllUserOrgIds(Long userId);

    /**
     * 根据组织id查询用户组织信息
     * @param page pageParam
     * @param pageReqVO pageReqVO
     * @return PageResult<OrgUserPageRespVO>
     */
    Page<OrgUserPageRespVO> getUserListByOrgId(@Param("page") Page<OrgUserPageRespVO> page, @Param("pageReqVO") com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageReqVO pageReqVO);

    /**
     * 批量删除组织用户
     * @param orgMoveReqVO orgMoveReqVO
     * @return int
     */
    int deleteBatchOrgUser(@Param("orgMoveReqVO") OrgUserMoveReqVO orgMoveReqVO);

    /**
     * 根据组织id查询用户组织信息
     * @param orgId orgId
     * @return List<OrgUserRespVO>
     */
    List<com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserRespVO> getUserListByUpdate(@Param("orgId") Long orgId);

    /**
     * 获取节点人员数量
     * @param unionIds orgIds
     * @return List
     */
    List<OrgUserNum> getNodeUserNum(@Param("unionIds") Collection<Long> unionIds);

    /**
     * 根据用户id/项目id查询用户组织id集合
     * @param userId userId
     * @return List<Long>
     */
    List<Long>   selectUserOrgIdsByBusinessId(Long userId,Long businessId);

    @DataPermission(enable = false) // 关闭数据权限
    default List<OrgUserDO> selectListByUserId(Long userId, Long businessId) {
        LambdaQueryWrapper<OrgUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrgUserDO::getUserId, userId);
        if (businessId != null) {
            queryWrapper.eq(OrgUserDO::getBusinessId, businessId);
        }
        return selectList(queryWrapper);
    }
}