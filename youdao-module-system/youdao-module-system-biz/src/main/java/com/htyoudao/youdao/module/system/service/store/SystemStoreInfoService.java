package com.htyoudao.youdao.module.system.service.store;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.api.store.dto.*;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSimpleResVO;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.PrintAllInfoVo;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.PrinterTomplateReqVO;
import com.htyoudao.youdao.module.system.controller.app.store.vo.*;
import com.htyoudao.youdao.module.system.controller.app.store.vo.DC.StoreInfoDCRespVo;
import com.htyoudao.youdao.module.system.controller.app.store.vo.StoreWecomConfigReqVO;
import com.htyoudao.youdao.module.system.controller.app.store.vo.StoreWecomConfigResVO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 门店 Service 接口
 *
 * @author 0090
 */
public interface SystemStoreInfoService {

    /**
     * 根据门店id查询所属组织id
     *
     * @param storeIds storeIds
     * @return orgIds
     */
    List<Long> selectStoreOrgIds(List<Long> storeIds);

    /**
     * 查询门店窗口分页
     *
     * @param pageReqVO pageReqVO
     * @return OrgStorePageReqVO
     */
    PageResult<OrgStorePageRespVO> selectPage(OrgStorePageReqVO pageReqVO);

    /**
     * 根据组织id查询门店列表
     *
     * @param orgId     orgId
     * @param storeName storeName
     * @return SystemStoreInfoDO
     */
    List<SystemStoreInfoDO> getStoreListByOrgId(Long orgId, String storeName);

    /**
     * 组织添加门店
     *
     * @param orgStoreSaveReqVO orgStoreSaveReqVO
     * @return Boolean
     */
    int addStoreForOrg(OrgStoreSaveReqVO orgStoreSaveReqVO);

    /**
     * 分页查询
     *
     * @param pageReqVO
     * @return
     */
    PageResult<StoreResVO> getStorePage(StorePageReqVO pageReqVO);

    /**
     * 添加门店
     *
     * @param storeSaveReqVO
     * @return
     */
    Long createStore(@Valid StoreSaveReqVO storeSaveReqVO);

    /**
     * 修改门店
     */
    void updateStore(@Valid StoreSaveReqVO storeSaveReqVO);

    /**
     * 删除门店
     */
    void deleteStore(Long storeId);

    /**
     * 修改门店状态
     */
    void updateOpenStatus(Long storeId, Integer status);

    /**
     * 生成二维码
     */
    void getStoreQRCode(Long storeId);

    /**
     * 下载二维码
     */
    String getWxaCodeByStoreId(Long storeId);

    /**
     * 门店详情
     */
    StoreDetailResVO getStoreDetail(Long storeId);

    /**
     * 门店移动
     *
     * @param orgStoreMoveReqVO orgStoreMoveReqVO
     * @return int
     */
    int moveStore(OrgStoreMoveReqVO orgStoreMoveReqVO);

    /**
     * 移除门店
     *
     * @param orgStoreMoveReqVO orgStoreMoveReqVO
     * @return int
     */
    int removeStore(OrgStoreMoveReqVO orgStoreMoveReqVO);

    /**
     * 通过组织id和门店ids查询门店列表
     *
     * @param orgId     orgId
     * @param storeName storeName
     * @param status    status
     * @return List<SystemStoreInfoDO>
     */
    List<SystemStoreInfoDO> getStoreListByOrgIdAndStoreId(Long orgId, String storeName, Integer status);

    /**
     * 获取已开启门店列表
     *
     * @param orgId     orgId
     * @param storeName storeName
     * @return List<SystemStoreInfoDO>
     */
    List<SystemStoreInfoDO> getOpenStoreListByOrgId(Long orgId, String storeName);

    /**
     * 获取节点门店数量
     *
     * @param unionIds orgIds
     * @return List
     */
    List<OrgStoreNum> getNodeStoreNum(Collection<Long> unionIds);

    /**
     * 判断组织下是否有门店
     *
     * @param id orgId
     * @return Boolean
     */
    Boolean hasOrgStore(Long id);

    /**
     * 修改门店负责人
     *
     * @param storeId     storeId
     * @param adminUserDO adminUserDO
     * @return int
     */
    int updateStoreLeader(Long storeId, AdminUserDO adminUserDO);

    /**
     * 根据组织查询所属门店id
     */
    Set<Long> getStoreIdsByOrgIds(Set<Long> orgId);

    /**
     * 撤销门店负责人
     *
     * @param storeId storeId
     * @return int
     */
    int unStoreManager(Long storeId);

    /**
     * 根据组织获取们带你
     */
    List<SystemStoreInfoDO> selectStoreByOrgIds(Set<Long> orgIds);


    /**
     * 获取未闭店的门店列表
     */
    List<SystemStoreInfoDO> selectByLetterStoreByOrgIds(Set<Long> orgIds);

    /**
     * 批量修改标签
     */
    void updateBatchTag(StoresUpdateVO storeUpdateVO);

    /**
     * 批量删除标签
     */
    void updateDelTag(StoresUpdateVO storeUpdateVO);

    /**
     * 根据标签查询门店
     */
    List<StoreResVO> selectStoreByTag(StoreTagReqVO storeTagReqVO);

    /**
     * 根据ids查询门店
     */
    List<StoreSimpleResVO> getStoreSimpleResDtoList(List<Long> storeIds);


    /**
     * 导出用户
     *
     * @param exportReqVO 导出条件
     * @param response    响应
     * @throws IOException IOException
     */
    void exportStoreList(StorePageReqVO exportReqVO, HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 根据用户经纬度获取距离用户最近的门店信息
     */
    List<StoreWecomConfigResVO> storeListByCity(StoreWecomConfigReqVO dto);

    /**
     * 根据当前登入人获取负责区域的门店或店长的门店
     */
    List<StoreSimpleResVO> storeListByUser();

    List<StoreInfoDTO> getStoreListByName(String storeName);

    List<StoreInfoDTO> getStoresByStoreIds(List<Long> storeIds);

    PrintAllInfoVo selectByStorePrinterInfo(PrinterTomplateReqVO printerTomplateReqVO);


    StoreInfoDCRespVo getStoreDetailForDc(Long id);

    /**
     * 根据组织id查询门店id
     *
     * @param orgId orgId
     * @return Long
     */
    List<Long> getStoreIdsByDeptId(Long orgId);

    /**
     * 查询全部门店列表id和name  微信用户导出用
     *
     * @return StoreInfoDTO
     */
    List<StoreInfoDTO> getAllStoreList();

    List<StoreInfoDTO> getStoreListByBusinessId(Long businessId);


    /**
     * 查询城市
     */
    List<SysDeptCityVO> getCityName(String deptName);

    /**
     * 根据门店id查询门店信息
     */
    StoreDTO getStoreByStoreId(Long storeId);

    StoreDTO getStoreByDouyinStoreId(Long douyinStoreId);

    List<Long> selectByOrgStoreList(Long orgId);

    PageResult<SystemStoreInfoDO> chooseStore(StorePageListReqVO pageReqVO);

    PageResult<SystemStoreInfoDO> chooseStoreForTiktok(StorePageListReqVO storePageListReqVO);

    StoreDeliveryDTO getStoreDelivery(Long storeId);

    List<StoreInfoDTO> getStoreIdsByOrgId(Long orgId);


    Set<SystemStoreInfoDO> selectOrgStoreList(Long orgId);

    List<StoreInfoDTO> getStoresByName(String storeName);

    List<StoreWecomConfigResDTO> selectByCouponStoreList(StoreWecomConfigReqDTO storeWecomConfigReqDTO);

    StoreWecomConfigResDTO getStoreById(Long storeId);

    List<StoreSimpleResVO> listSimple();

    List<StoreSimpleResVO> getStoreListByUser();

    List<OrgStoreDTO> getOrgIdsByStoreIds(List<Long> storeIds);

    List<SystemStoreInfoDO> selectStoreOrgInfos(List<Long> storeIds);

    List<Long> selectUserAllOrgStoreIds(Long userId);

    List<StoreSelectVO> getAllStoreInfo(String storeName);


    List<SystemStoreInfoDO> selectStoreByOrgIdsTwo(Set<Long> collect);

    List<Long> getStoreIdsByAllOrgId(Long orgId);

    void syncLocation();

    void fixStoreExpenses();

    /**
     * 根据组织获取们经营中门店
     */
    List<SystemStoreInfoDO> selectOperationStoreByOrgIds(Set<Long> orgIds);

    List<StoreSimpleResVO> getMaterialStoreList(String name, Long storeId, Long warehouseId);

    void updateStoreByDC(@Valid StoreSaveReqVO reqVO);


    List<Long> getStoreIdsByWarehouseId(Long warehouseId);

    List<SystemStoreInfoDO> selectAllStore(StorePageListReqVO reqVO);

    List<SystemStoreInfoDO> selectDouyinAllStore(StorePageListReqVO reqVO);

    List<StoreSimpleResDto> getStoreListByChannelId(String channelType, List<String> channelIds);

    StoreSimpleResVO getCheckOutId(int type, String outId, String storeId);


    /**
     * 根据用户id查询门店 app  oa
     *
     * @param userId userId
     * @return List<StoreResVO>
     */
    List<StoreResVO> getStoreListByUserId(Long userId);

    /**
     * 根据用户id查询门店 app  oa
     *
     * @param userId userId
     * @return List<StoreResVO>
     */
    List<SystemStoreInfoDO> getStoreDOListByUserId(Long userId);

    void updateStoreByBoss(@Valid StoreSaveReqVO reqVO);

    /**
     * 根据用户查询门店 供应链
     */
    List<StoreSimpleResVO> storeGylListByUser();


    Boolean isTag(Long storeId, Long tagId);


    List<StoreInfoDTO> selectAdvertisingStoreList(List<Long> tagList);

    PageResult<StoreInfoDTO> getStoreInfoByStoreIds(List<Long> storeIds, PageParam pageParam);

    void updateStoreStateByBoss(StoreUpdateStateReqVO reqVO);

    List<StoreSimpleResVO> storeListByBoss();


    List<StoreInfoDTO> listStoresByIds(List<Long> storeIds);

    List<StoreStatusLogDTO> listClosedLogs(List<Long> storeIds, LocalDateTime startInclusive, LocalDateTime endExclusive);

    List<StoreInfoDTO> listStoresByWarehouse(Long warehouseId, List<Long> storeIds);

    /**
     * 根据标签id查询门店id和门店名称
     * @param ids ids
     * @return Map<Long, String>
     */
    Map<Long, List<StoreInfoDTO>> getStoreIdsByTagIds(List<Long> ids);

    /**
     * 初始化门店标签 Redis 缓存，key 为 activity_tag{businessId}:{storeId}，Set 结构，member 为该门店的标签 id
     *
     * @return 是否初始化成功
     */
    Boolean initStoreTagCache();

    /**
     * 判断门店标签缓存是否命中：activity_tag{businessId}:{storeId} 的标签列表中是否包含入参标签中的任意一个
     *
     * @param businessId 项目编号
     * @param storeId    门店编号
     * @param tagIds     待判断的标签 id 列表
     * @return 命中任意一个标签返回 true
     */
    Boolean matchStoreTagCache(Long businessId, Long storeId, List<Long> tagIds);

    List<StoreLettersRespVO> getByLetterStoreList();

    /**
     * 获取带字母索引的门店列表
     * @param queryDTO 门店名称（模糊查询）
     */
    StoreListWithIndexVO getByLetterStoreListWithIndex(StoreQueryDTO queryDTO);

    /**
     * 当前登录人 根据组织架构 获取能查看的门店的接口 根据用户当前的经纬度，计算距离1km之内，并返回由近及远的门店列表
     * @param longitude longitude
     * @param latitude latitude
     * @return List<StoreItemVO>
     */
    List<StoreItemVO> getNearbyStores(String longitude, String latitude);

    /**
     * 获取当前登录人能查看的门店id
     * @return List<Long>
     */
    List<Long> getStoreIdsByUser();

    /**
     * 判断距离
     * @param storeId storeId
     * @param distance distance
     * @param longitude longitude
     * @param latitude latitude
     * @return Boolean
     */
    Boolean judgeDistance(String longitude, String latitude, Long storeId, double distance);

    /**
     * 获取门店基础信息
     * @param storeId storeId
     * @return StoreBasicInfoRespVO
     */
    StoreBasicInfoRespVO getStoreBasicInfoById(Long storeId);

    /**
     * 获取门店当前营业及外卖状态
     * @param storeId storeId
     * @return StoreBusinessStatusRespVO
     */
    StoreBusinessStatusRespVO getStoreBusinessStatus(Long storeId);
}
