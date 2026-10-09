package com.htyoudao.youdao.module.system.service.storeuser;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.StoreManagerReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.StoreUserRemoveReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.enums.org.OrgUserTypeConstants;
import com.htyoudao.youdao.module.system.enums.org.StoreUserTypeConstants;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import com.htyoudao.youdao.module.system.controller.admin.storeuser.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.storeuser.StoreUserDO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;

import com.htyoudao.youdao.module.system.dal.mysql.storeuser.StoreUserMapper;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;
import org.springframework.context.annotation.Lazy;

/**
 * 门店和用户关联 Service 实现类
 *
 * @author 零零玖零
 */
@Service
@Validated
public class StoreUserServiceImpl implements StoreUserService {

    @Resource
    private StoreUserMapper storeUserMapper;

    @Resource
    @Lazy
    private AdminUserService adminUserService;

    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    @Override
    public Long createStoreUser(StoreUserSaveReqVO createReqVO) {
        // 插入
        StoreUserDO storeUser = BeanUtils.toBean(createReqVO, StoreUserDO.class);
        storeUserMapper.insert(storeUser);
        // 返回
        return storeUser.getId();
    }

    @Override
    public void updateStoreUser(StoreUserSaveReqVO updateReqVO) {
        // 校验存在
        validateStoreUserExists(updateReqVO.getId());
        // 更新
        StoreUserDO updateObj = BeanUtils.toBean(updateReqVO, StoreUserDO.class);
        storeUserMapper.updateById(updateObj);
    }

    @Override
    public void deleteStoreUser(Long id) {
        // 校验存在
        validateStoreUserExists(id);
        // 删除
        storeUserMapper.deleteById(id);
    }

    private void validateStoreUserExists(Long id) {
        if (storeUserMapper.selectById(id) == null) {
            throw exception(STORE_USER_NOT_EXISTS);
        }
    }

    @Override
    public StoreUserDO getStoreUser(Long id) {
        return storeUserMapper.selectById(id);
    }

    @Override
    public PageResult<StoreUserDO> getStoreUserPage(StoreUserPageReqVO pageReqVO) {
        return storeUserMapper.selectPage(pageReqVO);
    }


    @Override
    public List<Long> selectUserStoreIds(Long userId) {
        QueryWrapper<StoreUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<StoreUserDO> storeUserDOList = storeUserMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(storeUserDOList)) {
            return storeUserDOList.stream().map(StoreUserDO::getStoreId).toList();
        }
        return List.of();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveBatch(List<StoreUserSaveReqVO> list,Long storeId) {
        //删除原有的  避免重复
        QueryWrapper<StoreUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_id", storeId);
        queryWrapper.in("user_id", list.stream().map(StoreUserSaveReqVO::getUserId).collect(Collectors.toList()));
        storeUserMapper.delete(queryWrapper);

        Set<StoreUserDO> collect = list.stream().map(item -> {
            StoreUserDO storeUserDO = new StoreUserDO();
            storeUserDO.setUserId(item.getUserId());
            storeUserDO.setStoreId(item.getStoreId());
            storeUserDO.setType(OrgUserTypeConstants.ORG_USER_TYPE_NORMAL);
            return storeUserDO;
        }).collect(Collectors.toSet());
        return storeUserMapper.insertBatch(collect);
    }

    @Override
    public Page<OrgUserPageRespVO> getUserListByStoreId(com.htyoudao.youdao.module.system.controller.admin.org.vo.StoreUserPageReqVO pageReqVO) {
        Page<OrgUserPageRespVO> objectPage = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        return storeUserMapper.getUserListByStoreId(objectPage, pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setStoreManager(StoreManagerReqVO storeManagerReqVO) {
        LambdaUpdateWrapper<StoreUserDO> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(StoreUserDO::getStoreId, storeManagerReqVO.getStoreId());
        queryWrapper.set(StoreUserDO::getType, StoreUserTypeConstants.STORE_USER_TYPE_NORMAL);
        storeUserMapper.update(queryWrapper);
        queryWrapper.clear();
        queryWrapper.eq(StoreUserDO::getStoreId, storeManagerReqVO.getStoreId());
        queryWrapper.eq(StoreUserDO::getUserId, storeManagerReqVO.getUserId());
        queryWrapper.set(StoreUserDO::getType, StoreUserTypeConstants.STORE_USER_TYPE_PRINCIPAL);
        queryWrapper.set(StoreUserDO::getVisible, StoreUserTypeConstants.STORE_USER_VISIBLE);

        // 更新store表的 店长字段
        AdminUserDO adminUserDO = adminUserService.getUser(storeManagerReqVO.getUserId());
        systemStoreInfoService.updateStoreLeader(storeManagerReqVO.getStoreId(), adminUserDO);
        return storeUserMapper.update(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unStoreManager(StoreManagerReqVO storeManagerReqVO) {
        LambdaUpdateWrapper<StoreUserDO> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(StoreUserDO::getStoreId, storeManagerReqVO.getStoreId());
        queryWrapper.eq(StoreUserDO::getUserId, storeManagerReqVO.getUserId());
        queryWrapper.set(StoreUserDO::getType, StoreUserTypeConstants.STORE_USER_TYPE_NORMAL);
        systemStoreInfoService.unStoreManager(storeManagerReqVO.getStoreId());
        return storeUserMapper.update(queryWrapper);
    }

    @Override
    public int removeStoreUser(StoreUserRemoveReqVO storeUserRemoveReqVO) {
        QueryWrapper<StoreUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_id", storeUserRemoveReqVO.getStoreId());
        queryWrapper.in("user_id", storeUserRemoveReqVO.getUserIds());
        StoreUserDO storeUserDO = storeUserMapper.selectOne(queryWrapper);
        Integer type = storeUserDO.getType();
        if(type.equals(StoreUserTypeConstants.STORE_USER_TYPE_PRINCIPAL)){
            systemStoreInfoService.unStoreManager(storeUserRemoveReqVO.getStoreId());
        }
        return storeUserMapper.delete(queryWrapper);
    }

    @Override
    public int storeSetUserIsVisible(StoreManagerReqVO storeManagerReqVO) {
        LambdaUpdateWrapper<StoreUserDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StoreUserDO::getStoreId, storeManagerReqVO.getStoreId());
        updateWrapper.eq(StoreUserDO::getUserId, storeManagerReqVO.getUserId());
        updateWrapper.set(StoreUserDO::getVisible, StoreUserTypeConstants.STORE_USER_VISIBLE);
        return storeUserMapper.update(updateWrapper);
    }

    @Override
    public int storeSetUserUnVisible(StoreManagerReqVO storeManagerReqVO) {
        LambdaQueryWrapper<StoreUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoreUserDO::getStoreId, storeManagerReqVO.getStoreId());
        queryWrapper.eq(StoreUserDO::getUserId, storeManagerReqVO.getUserId());
        StoreUserDO storeUserDO = storeUserMapper.selectOne(queryWrapper);
        if(storeUserDO.getType().equals(StoreUserTypeConstants.STORE_USER_TYPE_PRINCIPAL)){
            throw exception(ORG_STORE_LEADER_VISIBLE);
        }
        LambdaUpdateWrapper<StoreUserDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StoreUserDO::getStoreId, storeManagerReqVO.getStoreId());
        updateWrapper.eq(StoreUserDO::getUserId, storeManagerReqVO.getUserId());
        updateWrapper.set(StoreUserDO::getVisible, StoreUserTypeConstants.STORE_USER_NOT_VISIBLE);
        return storeUserMapper.update(updateWrapper);
    }

    @Override
    public List<Long> selectUserStoreIdsTwo(Long userId) {
        QueryWrapper<StoreUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<StoreUserDO> storeUserDOList = storeUserMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(storeUserDOList)) {
            return storeUserDOList.stream().map(StoreUserDO::getStoreId).toList();
        }
        return List.of();
    }
}