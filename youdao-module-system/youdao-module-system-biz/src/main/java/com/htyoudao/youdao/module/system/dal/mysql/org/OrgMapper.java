package com.htyoudao.youdao.module.system.dal.mysql.org;

import java.util.*;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 组织机构 Mapper
 *
 * @author 零零玖零
 */
@Mapper
public interface OrgMapper extends BaseMapperX<OrgDO> {

    /**
     * 分页查询
     * @param reqVO reqVO
     * @return PageResult<OrgDO>
     */
    default PageResult<OrgDO> selectPage(OrgPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OrgDO>()
                .likeIfPresent(OrgDO::getName, reqVO.getName())
                .eqIfPresent(OrgDO::getAncestors, reqVO.getAncestors())
                .eqIfPresent(OrgDO::getParentId, reqVO.getParentId())
                .eqIfPresent(OrgDO::getSort, reqVO.getSort())
                .eqIfPresent(OrgDO::getType, reqVO.getType())
                .eqIfPresent(OrgDO::getCode, reqVO.getCode())
                .eqIfPresent(OrgDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OrgDO::getLevel, reqVO.getLevel())
                .eqIfPresent(OrgDO::getBusinessId, reqVO.getBusinessId())
                .betweenIfPresent(OrgDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OrgDO::getId));
    }


    /**
     * 目前没用
     * @param userId userId
     * @param userStoreIds userStoreIds
     * @return List<OrgTreeRespVO>
     */
    List<OrgTreeRespVO> selectAccOrgList(@Param("userId") Long userId,@Param("userId") List<Long> userStoreIds);

    /**
     * 根据id集合查询子节点
     * @param allOrgIds allOrgIds
     * @return List<OrgDO>
     */
    List<OrgDO> selectChildByIds(@Param("allOrgIds") Set<Long> allOrgIds);

    /**
     * 根据组织 ID 集合一次查询组织自身及全部子组织。
     *
     * @param allOrgIds 组织 ID 集合
     * @return 组织自身及全部子组织
     */
    List<OrgDO> selectSelfAndChildrenByIds(@Param("allOrgIds") Set<Long> allOrgIds);

    /** 查询组织自身及全部子组织ID，避免加载完整组织信息。 */
    List<Long> selectSelfAndChildrenIdsByIds(@Param("allOrgIds") Set<Long> allOrgIds);


    @Select("select distinct org_id from system_org_user sou where user_id = #{userId} and deleted = b'0'")
    List<Long> orgByUserId(@Param("userId")Long userId);

    @Select("select distinct id from system_org where FIND_IN_SET(#{orgId}, ancestors) > 0;")
    Set<Long> getChildIdList(@Param("orgId")Long orgId);

    /**
     * 根据id查询子节点
     * @param orgId orgId
     * @return Set<OrgDO>
     */
    @Select("select distinct * from system_org where deleted =b'0' and FIND_IN_SET(#{orgId}, ancestors) > 0")
    Set<OrgDO> getChildOrgList(@Param("orgId") Long orgId);


    /**
     * 根据id查询子节点ids
     * @param orgId
     * @return Set<Long>
     */
    @Select("select distinct id from system_org where deleted =b'0' and FIND_IN_SET(#{orgId}, ancestors) > 0")
    Set<Long> getChildOrgIdList(@Param("orgId") Long orgId);

    /**
     * 更新父节点和祖先节点
     * @param id id
     * @param newOrgId newOrgId
     * @param newAncestorPrefix newAncestorPrefix
     * @param maxSort maxSort
     * @return int
     */
    int updateParentAndAncestors(@Param("id") Long id,@Param("newOrgId") Long newOrgId,@Param("newAncestorPrefix") String newAncestorPrefix,@Param("maxSort") Integer maxSort);

    /**
     * 批量更新子节点的祖先节点
     * @param allOrgIds allOrgIds
     * @param oldAncestorPrefix oldAncestorPrefix
     * @param newAncestorPrefix newAncestorPrefix
     * @return int
     */
    int batchUpdateChildrenAncestors(@Param("allOrgIds") Set<Long> allOrgIds,@Param("oldAncestorPrefix") String oldAncestorPrefix,@Param("newAncestorPrefix") String newAncestorPrefix);

    /**
     * 根据用户id查询用户所属的组织名称
     * @param userIds userIds
     * @return List<OrgUserVO>
     */
    List<OrgUserVO> getOrgNameByUserIds(@Param("userIds") Set<Long> userIds);

    /**
     * 判断是否有子节点(排除自己)
     * @param id id
     * @return Set
     */
    Set<Long> hasChildOrg(@Param("id") Long id);

    /**
     * 根据祖级节点 删除
     * @param ancestors 祖级节点
     */
    void deleteByAnce(String ancestors);
    @DataPermission(enable = false)
    default List<OrgDO> selectList(Set<Long> businessIds) {
        return selectList(new LambdaQueryWrapperX<OrgDO>()
                .inIfPresent(OrgDO::getBusinessId, businessIds)
                .orderByDesc(OrgDO::getId));
    }
    @Select("select distinct id from system_org where FIND_IN_SET(#{orgId}, ancestors) > 0;")
    @DataPermission(enable = false)
    Set<Long> getChildIdListByStore(@Param("orgId")Long orgId);

}
