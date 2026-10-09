package com.htyoudao.youdao.module.system.service.org;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.api.org.dto.StoreOrgDTO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.orguser.vo.OrgUserSaveReqVO;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.AppOrgRespVO;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.UserDeptInfoPageRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;

/**
 * 组织机构 Service 接口
 *
 * @author 零零玖零
 */
public interface OrgService {

    /**
     * 创建组织机构
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOrg(@Valid OrgSaveReqVO createReqVO);

    /**
     * 更新组织机构
     *
     * @param updateReqVO 更新信息
     */
    void updateOrg(@Valid OrgSaveReqVO updateReqVO);

    /**
     * 删除组织机构
     *
     * @param id 编号
     */
    void deleteOrg(Long id);

    /**
     * 获得组织机构
     * 目前没用
     * @param id 编号
     * @return 组织机构
     */
    OrgDO getOrg(Integer id);

    /**
     * 获得组织机构分页
     * 目前没用
     * @param pageReqVO 分页查询
     * @return 组织机构分页
     */
    PageResult<OrgDO> getOrgPage(OrgPageReqVO pageReqVO);

    /**
     * 查询组织树集合
     * v1 需要通过用户和组织结构的关系
     * @return 组织机构树集合
     */
    List<OrgTreeRespVO> getOrgListByUser();

    /**
     * 查询组织树集合
     * v2 不需要通过用户和组织结构的关系
     * @return 组织机构树集合
     */
    List<OrgTreeRespVO> getOrgListByUserV2();

    Set<Long> getChildIdList(Long orgId);

    List<Long> orgIdByUserId(Long userId);

    /**
     * 移动组织机构
     * @param orgMoveReqVO orgMoveReqVO
     * @return int
     */
    int moveOrg(OrgMoveReqVO orgMoveReqVO);

    /**
     * 添加下级组织时候选择负责人的窗口
     * @param pageReqVO pageReqVO
     * @return Page<OrgUserPageRespVO>
     */
    PageResult<OrgUserPageRespVO> getUserListWithOrg(OrgUserPageReqVO pageReqVO);

    /**
     * 添加(门店)的窗口
     * @param pageReqVO pageReqVO
     * @return OrgStorePageReqVO
     */
    PageResult<OrgStorePageRespVO> getStoreListWithOrg(OrgStorePageReqVO pageReqVO);

    /**
     * 通过组织查询门店
     * @param orgId orgId
     * @param storeName storeName
     * @param status status
     * @return OrgStoreRespVO
     */
    List<OrgStoreRespVO> getStoreListByOrgId(Long orgId,String storeName,Integer status);

    /**
     * 通过组织查询门店，显示已开启的
     * @param orgId orgId
     * @param storeName storeName
     * @return OrgStoreRespVO
     */
    List<OrgStoreRespVO> getOpenStoreListByOrgId(Long orgId, String storeName);

    /**
     * 组织添加门店
     * @param orgStoreSaveReqVO orgStoreSaveReqVO
     * @return Boolean
     */
    int addStoreForOrg(OrgStoreSaveReqVO orgStoreSaveReqVO);

    /**
     * 组织添加人员
     * @param orgUserSaveReqVO orgStoreSaveReqVO
     * @return Boolean
     */
    Boolean addUserForOrg(OrgUserSaveReqVO orgUserSaveReqVO);

    /**
     * 移动人员
     * @param orgMoveReqVO orgMoveReqVO
     * @return Boolean
     */
    Boolean moveOrgUser(@Valid OrgUserMoveReqVO orgMoveReqVO);

    /**
     * 移除人员
     * @param orgMoveReqVO orgMoveReqVO
     * @return int
     */
    int removeOrgUser(@Valid OrgUserMoveReqVO orgMoveReqVO);

    /**
     * 设置负责人
     * @param orgUserChargedReqVO orgUserChargedReqVO
     * @return int
     */
    int setCharged(@Valid OrgUserChargedReqVO orgUserChargedReqVO);

    /**
     * 撤销负责人
     * @param orgUserChargedReqVO orgUserChargedReqVO
     * @return int
     */
    int unCharged(@Valid OrgUserChargedReqVO orgUserChargedReqVO);

    /**
     * 通过组织查询当前节点人员
     * @param pageReqVO pageReqVO
     * @return OrgUserPageRespVO
     */
    PageResult<OrgUserPageRespVO> getUserListByOrgId(OrgUserPageReqVO pageReqVO);

    /**
     * 通过组织查询当前节点人员
     * @param orgId orgId
     * @return OrgUserRespVO
     */
    List<OrgUserRespVO> getUserListByUpdate(Long orgId);

    /**
     * 门店添加人员
     * @param orgStoreSaveReqVO orgStoreSaveReqVO
     * @return int
     */
    Boolean addUserForStore(StoreUserSaveBatchReqVO orgStoreSaveReqVO);

    /**
     * 通过门店查询人员
     * @param pageReqVO pageReqVO
     * @return PageResult
     */
    PageResult<OrgUserPageRespVO> getUserListByStoreId(StoreUserPageReqVO pageReqVO);

    /**
     * 门店添加人员时候的查询窗口
     * @param pageReqVO pageReqVO
     * @return PageResult
     */
    PageResult<OrgUserPageRespVO> getUserListWithStore(OrgUserPageReqVO pageReqVO);

    /**
     * 移动门店
     * @param orgStoreMoveReqVO orgStoreMoveReqVO
     * @return Integer
     */
    Integer moveStore(OrgStoreMoveReqVO orgStoreMoveReqVO);

    /**
     * 移除门店
     * @param orgStoreMoveReqVO orgStoreMoveReqVO
     * @return Integer
     */
    Integer removeStore(OrgStoreMoveReqVO orgStoreMoveReqVO);

    /**
     * 设置门店店长
     * @param storeManagerReqVO storeManagerReqVO
     * @return Boolean
     */
    int setStoreManager(@Valid StoreManagerReqVO storeManagerReqVO);

    /**
     * 撤销门店店长
     * @param storeManagerReqVO storeManagerReqVO
     * @return Boolean
     */
    int unStoreManager(@Valid StoreManagerReqVO storeManagerReqVO);

    /**
     * 移除门店人员
     * @param storeUserRemoveReqVO storeUserRemoveReqVO
     * @return Integer
     */
    int removeStoreUser(@Valid StoreUserRemoveReqVO storeUserRemoveReqVO);

    /**
     * 组织机构树集合 根据当前登入人以及项目
     *
     * @return 组织机构树集合
     */
    List<OrgTreeRespVO> getOrgListByUserAndBusID(Long businessId);
    /**
     * 根据当前登入人以及项目 获取本级组织以及下级组织id
     */
    Set<Long> getStoreIdsByUser();
    Set<Long> getStoreIdsByUserRpc(Long businessId);
    /**
     * 根据当前登录人/所选项目 获取组织门店树
     *
     * @return 获取组织门店树
     */
    List<OrgStoreTreeRespVO> getOrgStoreListByUserAndBusID();
    /**
     *  根据项目id 获取下级组织id
     *
     * @return 获取组织门店
     */
    Set<Long> getStoreIdListByOrgID(Long orgId);
    /**
     *  根据项目id 获取下级组织id
     *
     * @return 获取组织门店
     */
    Set<Long> getStoreIdListByOrgIDRpc(Long orgId,Long businessId);

    /**
     * 门店设置用户可见
     * @param storeManagerReqVO storeManagerReqVO
     * @return Integer
     */
    int storeSetUserIsVisible(StoreManagerReqVO storeManagerReqVO);

    /**
     * 门店设置用户不可见
     * @param storeManagerReqVO storeManagerReqVO
     * @return Integer
     */
    int storeSetUserUnVisible(StoreManagerReqVO storeManagerReqVO);
    /**
     * 根据项目id 向下递归所有项目信息
     * @param orgId
     * @return
     */
    Set<OrgDO> getChildOrgList(Long orgId);

    /**
     * 组织设置用户可见
     * @param orgUserChargedReqVO orgUserChargedReqVO
     * @return Integer
     */
    int orgSetUserIsVisible(OrgUserChargedReqVO orgUserChargedReqVO);

    /**
     * 组织设置用户不可见
     * @param orgUserChargedReqVO orgUserChargedReqVO
     * @return Integer
     */
    int orgSetUserUnVisible(OrgUserChargedReqVO orgUserChargedReqVO);

    List<OrgTreeRespVO> getOrgListByUsers();

    List<OrgStoreTreeRespVO> getOrgStoreListByUserData();

    List<OrgStoreCityTreeRespVO> getOrgStoreByCity();

    /**
     * 根据门店id 获取门店组织信息
     * @param storeIds storeIds
     * @return StoreOrgDTO
     */
    Set<StoreOrgDTO> getOrgListByStoreId(List<Long> storeIds);

    /**
     * 根据门店id 获取门店组织信息
     * @param storeIds storeIds
     * @return StoreOrgDTO
     */
    Set<StoreOrgDTO> getOrgListByStoreIdV2(List<Long> storeIds);

    /**
     * 获取所有门店组织信息
     * @return StoreOrgDTO
     */
    Set<StoreOrgDTO> getAllOrgListByStoreIdV2();

    List<OrgStoreTreeRespVO> getAllOrgStoreList(Boolean filterClosedStore);

    List<OrgTreeRespVO> getAllOrgList();

    /**
     * 根据当前登录人获取组织树+门
     * @return OrgTreeRespVO
     */
    List<OrgTreeRespVO> getOrganizationsByLoginUser();

    /**
     * 根据当前登录人获取组织树+门店
     * @return OrgTreeRespVO
     */
    List<OrgTreeRespVO> getOrgAndStoreByLoginUser();


    List<OrgStoreTreeRespVO> getOperationStoreList();

    List<OrgStoreTreeRespVO> getOperationStoreListByUserData();

    /**
     * app 获取所有组织
     * @param businessId businessId
     * @return List
     */
    List<AppOrgRespVO> getAllOrg(Long businessId);

    /**
     * 排序组织
     * @param sortOrgList sortOrgList
     * @return Boolean
     */
    Boolean sortOrg(List<SortOrgReqVO> sortOrgList);

    AdminUserDO getOrgLeaderByOrgId(Long orgId);
}
