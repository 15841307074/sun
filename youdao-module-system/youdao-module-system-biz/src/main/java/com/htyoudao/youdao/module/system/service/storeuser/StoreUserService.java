package com.htyoudao.youdao.module.system.service.storeuser;

import java.util.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.StoreManagerReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.StoreUserRemoveReqVO;
import jakarta.validation.*;
import com.htyoudao.youdao.module.system.controller.admin.storeuser.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.storeuser.StoreUserDO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;

/**
 * 门店和用户关联 Service 接口
 *
 * @author 零零玖零
 */
public interface StoreUserService {

    /**
     * 创建门店和用户关联
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStoreUser(@Valid StoreUserSaveReqVO createReqVO);

    /**
     * 更新门店和用户关联
     *
     * @param updateReqVO 更新信息
     */
    void updateStoreUser(@Valid StoreUserSaveReqVO updateReqVO);

    /**
     * 删除门店和用户关联
     *
     * @param id 编号
     */
    void deleteStoreUser(Long id);

    /**
     * 获得门店和用户关联
     *
     * @param id 编号
     * @return 门店和用户关联
     */
    StoreUserDO getStoreUser(Long id);

    /**
     * 获得门店和用户关联分页
     *
     * @param pageReqVO 分页查询
     * @return 门店和用户关联分页
     */
    PageResult<StoreUserDO> getStoreUserPage(StoreUserPageReqVO pageReqVO);


    /**
     * 当前用户的店铺ID
     *
     * @param userId userId
     * @return 门店ids
     */
    List<Long> selectUserStoreIds(Long userId);

    /**
     * 批量保存
     *
     * @param list userId
     * @param storeId storeId
     * @return Boolean
     */
    Boolean saveBatch(List<StoreUserSaveReqVO> list,Long storeId);

    /**
     * 通过门店查询人员
     *
     * @param pageReqVO pageReqVO
     * @return OrgUserPageRespVO
     */
    Page<OrgUserPageRespVO> getUserListByStoreId(com.htyoudao.youdao.module.system.controller.admin.org.vo.StoreUserPageReqVO pageReqVO);

    /**
     * 设置店长
     * @param storeManagerReqVO storeManagerReqVO
     * @return Boolean
     */
    int setStoreManager(StoreManagerReqVO storeManagerReqVO);

    /**
     * 撤销店长
     * @param storeManagerReqVO storeManagerReqVO
     * @return Boolean
     */
    int unStoreManager(StoreManagerReqVO storeManagerReqVO);

    /**
     * 移除门店人员
     * @param storeUserRemoveReqVO storeUserRemoveReqVO
     * @return int
     */
    int removeStoreUser(StoreUserRemoveReqVO storeUserRemoveReqVO);

    /**
     * 设置门店人员可见
     * @param storeManagerReqVO storeManagerReqVO
     * @return int
     */
    int storeSetUserIsVisible(StoreManagerReqVO storeManagerReqVO);

    /**
     * 设置门店人员不可见
     * @param storeManagerReqVO storeManagerReqVO
     * @return int
     */
    int storeSetUserUnVisible(StoreManagerReqVO storeManagerReqVO);

    List<Long> selectUserStoreIdsTwo(Long userId);
}