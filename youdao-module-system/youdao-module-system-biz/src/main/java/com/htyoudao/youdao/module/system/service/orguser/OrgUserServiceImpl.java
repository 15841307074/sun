package com.htyoudao.youdao.module.system.service.orguser;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.orguser.vo.OrgUserPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.orguser.vo.OrgUserSaveReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.orguser.OrgUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.orguser.OrgUserMapper;
import com.htyoudao.youdao.module.system.enums.org.OrgUserTypeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.ORG_LEADER_VISIBLE;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.ORG_USER_NOT_EXISTS;

/**
 * 组织和用户关联 Service 实现类
 *
 * @author 零零玖零
 */
@Service
@Validated
public class OrgUserServiceImpl implements OrgUserService {

    @Resource
    private OrgUserMapper orgUserMapper;

    @Override
    public Long createOrgUser(OrgUserSaveReqVO createReqVO) {
        // 插入
        OrgUserDO orgUser = BeanUtils.toBean(createReqVO, OrgUserDO.class);
        orgUserMapper.insert(orgUser);
        // 返回
        return orgUser.getId();
    }

    @Override
    public void updateOrgUser(OrgUserSaveReqVO updateReqVO) {
        // 校验存在
        validateOrgUserExists(updateReqVO.getId());
        // 更新
        OrgUserDO updateObj = BeanUtils.toBean(updateReqVO, OrgUserDO.class);
        orgUserMapper.updateById(updateObj);
    }

    @Override
    public void deleteOrgUser(Long id) {
        // 校验存在
        validateOrgUserExists(id);
        // 删除
        orgUserMapper.deleteById(id);
    }

    private void validateOrgUserExists(Long id) {
        if (orgUserMapper.selectById(id) == null) {
            throw exception(ORG_USER_NOT_EXISTS);
        }
    }

    @Override
    public OrgUserDO getOrgUser(Long id) {
        return orgUserMapper.selectById(id);
    }

    @Override
    public PageResult<OrgUserDO> getOrgUserPage(OrgUserPageReqVO pageReqVO) {
        return orgUserMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Long> selectUserOrgIds(Long userId) {
        return orgUserMapper.selectUserOrgIds(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setCharged(OrgUserChargedReqVO orgUserChargedReqVO) {
        Long orgId = orgUserChargedReqVO.getOrgId();
        LambdaUpdateWrapper<OrgUserDO> updateWrapper = Wrappers.lambdaUpdate(OrgUserDO.class)
                .eq(OrgUserDO::getOrgId, orgId)
                .set(OrgUserDO::getType, OrgUserTypeConstants.ORG_USER_TYPE_NORMAL);
        orgUserMapper.update(updateWrapper);
        Long orgUserId = orgUserChargedReqVO.getOrgUserId();
        LambdaUpdateWrapper<OrgUserDO> updateWrapper1 = Wrappers.lambdaUpdate(OrgUserDO.class)
                .eq(OrgUserDO::getId, orgUserId)
                .set(OrgUserDO::getType, OrgUserTypeConstants.ORG_USER_TYPE_PRINCIPAL)
                .set(OrgUserDO::getVisible, OrgUserTypeConstants.ORG_USER_VISIBLE);
        return orgUserMapper.update(updateWrapper1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setChargedByUpdate(Long id, Long userId) {
        LambdaUpdateWrapper<OrgUserDO> updateWrapper = Wrappers.lambdaUpdate(OrgUserDO.class)
                .eq(OrgUserDO::getOrgId, id)
                .set(OrgUserDO::getType, OrgUserTypeConstants.ORG_USER_TYPE_NORMAL);
        orgUserMapper.update(updateWrapper);
        LambdaUpdateWrapper<OrgUserDO> updateWrapper1 = Wrappers.lambdaUpdate(OrgUserDO.class)
                .eq(OrgUserDO::getOrgId, id)
                .eq(OrgUserDO::getUserId, userId)
                .set(OrgUserDO::getType, OrgUserTypeConstants.ORG_USER_TYPE_PRINCIPAL)
                .set(OrgUserDO::getVisible, OrgUserTypeConstants.ORG_USER_VISIBLE);
        return orgUserMapper.update(updateWrapper1);
    }

    @Override
    public int unCharged(OrgUserChargedReqVO orgUserChargedReqVO) {
        LambdaUpdateWrapper<OrgUserDO> updateWrapper1 = Wrappers.lambdaUpdate(OrgUserDO.class)
                .eq(OrgUserDO::getId, orgUserChargedReqVO.getOrgUserId())
                .set(OrgUserDO::getType, OrgUserTypeConstants.ORG_USER_TYPE_NORMAL);
        return orgUserMapper.update(updateWrapper1);
    }

    @Override
    public Page<OrgUserPageRespVO> getUserListByOrgId(com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageReqVO pageReqVO) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageReqVO.getPageNo());
        pageParam.setPageSize(pageReqVO.getPageSize());
        return orgUserMapper.getUserListByOrgId(new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
    }

    @Override
    public int deleteBatchOrgUser(OrgUserMoveReqVO orgMoveReqVO) {
        return orgUserMapper.deleteBatchOrgUser(orgMoveReqVO);
    }

    @Override
    public Boolean insertBatch(List<OrgUserSaveReqVO> list) {
        List<OrgUserDO> insertList = BeanUtils.toBean(list, OrgUserDO.class);
        return orgUserMapper.insertBatch(insertList);
    }

    @Override
    public List<OrgUserRespVO> getUserListByUpdate(Long orgId) {
        return orgUserMapper.getUserListByUpdate(orgId);
    }

    @Override
    public Boolean saveBatch(OrgUserSaveReqVO orgUserSaveReqVO) {
        List<Long> userIds = orgUserSaveReqVO.getUserIds();
        // 先删除后添加
        OrgUserMoveReqVO orgMoveReqVO = new OrgUserMoveReqVO();
        orgMoveReqVO.setOldOrgId(orgUserSaveReqVO.getOrgId());
        orgMoveReqVO.setUserIds(userIds);
        orgUserMapper.deleteBatchOrgUser(orgMoveReqVO);

        List<OrgUserDO> orgUserDOList = new ArrayList<>();
        for (Long userId : userIds) {
            OrgUserDO orgUserDO = new OrgUserDO();
            orgUserDO.setUserId(userId);
            orgUserDO.setOrgId(orgUserSaveReqVO.getOrgId());
            orgUserDO.setType(OrgUserTypeConstants.ORG_USER_TYPE_NORMAL);
            orgUserDOList.add(orgUserDO);
        }
        return orgUserMapper.insertBatch(orgUserDOList);
    }

    @Override
    public List<OrgUserNum> getNodeUserNum(Collection<Long> unionIds) {
        return orgUserMapper.getNodeUserNum(unionIds);
    }

    @Override
    public Boolean hasOrgUser(Long id) {
        QueryWrapper<OrgUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_id", id);
        return orgUserMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<Long> selectUserOrgIdsByBusinessId(Long userId,Long businessId) {
        return orgUserMapper.selectUserOrgIdsByBusinessId(userId,businessId);
    }

    @Override
    public int orgSetUserIsVisible(OrgUserChargedReqVO orgUserChargedReqVO) {
        LambdaUpdateWrapper<OrgUserDO> updateWrapper = Wrappers.lambdaUpdate(OrgUserDO.class)
                .eq(OrgUserDO::getId, orgUserChargedReqVO.getOrgUserId())
                .set(OrgUserDO::getVisible, OrgUserTypeConstants.ORG_USER_VISIBLE);
        return orgUserMapper.update(updateWrapper);
    }

    @Override
    public int orgSetUserUnVisible(OrgUserChargedReqVO orgUserChargedReqVO) {
//        LambdaQueryWrapper<OrgUserDO> queryWrapper = Wrappers.lambdaQuery(OrgUserDO.class)
//                .eq(OrgUserDO::getId, orgUserChargedReqVO.getOrgUserId());
//        OrgUserDO orgUserDO = orgUserMapper.selectOne(queryWrapper);
//        Integer type = orgUserDO.getType();
//        //  负责必须可见
//        if(type.equals(OrgUserTypeConstants.ORG_USER_TYPE_PRINCIPAL)){
//            throw exception(ORG_LEADER_VISIBLE);
//        }
        LambdaUpdateWrapper<OrgUserDO> updateWrapper = Wrappers.lambdaUpdate(OrgUserDO.class)
                .eq(OrgUserDO::getId, orgUserChargedReqVO.getOrgUserId())
                .set(OrgUserDO::getVisible, OrgUserTypeConstants.ORG_USER_NOT_VISIBLE);
        return orgUserMapper.update(updateWrapper);
    }

    @Override
    public Long getOrgLeaderIdByOrgId(Long orgId) {
        LambdaQueryWrapper<OrgUserDO> queryWrapper = Wrappers.lambdaQuery(OrgUserDO.class)
                .eq(OrgUserDO::getOrgId, orgId)
                .eq(OrgUserDO::getType, OrgUserTypeConstants.ORG_USER_TYPE_PRINCIPAL);
        OrgUserDO orgUserDO = orgUserMapper.selectOne(queryWrapper);
        if(ObjectUtil.isEmpty(orgUserDO)){
            return null;
        }
        return orgUserDO.getUserId() == null ? null : orgUserDO.getUserId();
    }
}