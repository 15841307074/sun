package com.htyoudao.youdao.module.system.dal.mysql.store;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgStoreDTO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgStoreNum;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StorePageListReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StorePageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StorePageResVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreResVO;
import com.htyoudao.youdao.module.system.controller.admin.wxstore.vo.StoreWecomConfigRespVO;
import com.htyoudao.youdao.module.system.controller.admin.wxstore.vo.StoreWecomPageReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 门店 Mapper
 *
 * @author 0090
 */
@Mapper
public interface SystemStoreInfoMapper extends BaseMapperX<SystemStoreInfoDO> {

    /**
     * 根据门店id查询所属组织id
     * @param storeIds storeIds
     * @return List
     */
    List<Long> selectStoreOrgIds(@Param("storeIds") List<Long> storeIds);


    default List<Long> selectStoreIdsByOrgIds(@Param("orgIds") Collection<Long> orgIds){
        QueryWrapper<SystemStoreInfoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SystemStoreInfoDO::getOrgId, orgIds);
        return selectList(queryWrapper).stream().map(SystemStoreInfoDO::getStoreId).toList();
    }


    default Long selectCountByBusinessId(Long businessId) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemStoreInfoDO::getBusinessId, businessId);
        return selectCount(queryWrapper);
    }

    /**
     * 根据组织id查询所属门店数量
     * @param unionIds orgIds
     * @return List
     */
    List<OrgStoreNum> getNodeStoreNum(@Param("unionIds") Collection<Long> unionIds);

    @DataPermission(enable = false)
    IPage<StoreResVO> selectPageList(Page<StoreResVO> page, @Param("record") StorePageReqVO reqVO,@Param("businessId") Long businessId,@Param("userId") Long userId);
    /**
     *
     * 根据标签查询门店
     * @return
     */
    @DataPermission(enable = false)
    List<StoreResVO> selectStoreByTag( @Param("type") Integer type, @Param("tagIds") List<Long> tagIds,@Param("tagIdSize") int  tagIdSize,@Param("businessId") Long businessId,@Param("orgIds") Set<Long> orgIds);
    List<OrgDO> selectCityList(@Param("dept") OrgDO dept);


    IPage<StorePageResVO> selectStorePageList(Page<StorePageResVO> page, @Param("record")StorePageListReqVO record);

    IPage<StorePageResVO> selectStorePageListTwo(Page<StorePageResVO> page, @Param("record")StorePageListReqVO record);

    @DataPermission(enable = false)
    IPage<StoreWecomConfigRespVO> selectWecomPageList(Page<StoreWecomConfigRespVO> page, @Param("record") StoreWecomPageReqVO reqVO, @Param("businessId") Long businessId, @Param("userId") Long userId);


    /**
     * 根据门店id查询所属组织id
     * @param storeIds storeIds
     * @return List<OrgStoreDTO>
     */
    List<OrgStoreDTO> getOrgIdsByStoreIds(@Param("storeIds") List<Long> storeIds);

    /**
     * 根据当前用户查询组织范围内所有门店
     * @param userId
     * @return
     */
    List<Long> selectUserAllOrgStoreIds(Long userId);

    /**
     * 根据组织id查询所有门店
     * @param orgId orgId
     * @return List<Long>
     */
    List<Long> getStoreIdsByAllOrgId(Long orgId);

    /**
     * 查询所有门店
     */
    @DataPermission(enable = false)
    default List<SystemStoreInfoDO> selectAllStoreList(){
        return selectList();
    };
    /**
     * 查询供应链门店
     */
    @DataPermission(enable = false)
    default List<SystemStoreInfoDO> selectAllStoreByGylList(Long userId,Long businessId){
        return selectList(    new LambdaQueryWrapperX<SystemStoreInfoDO>()
                .eq(SystemStoreInfoDO::getUserId, userId)
                .eq(SystemStoreInfoDO::getStoreStatus, 0)
                .eq(SystemStoreInfoDO::getDeleted, 0)
                // 将所有businessId相关的OR条件包裹在同一个lambda中，保证逻辑正确性
                .and(wrapper -> wrapper
                        .eq(SystemStoreInfoDO::getBusinessId, 11L)
                        .or().eq(SystemStoreInfoDO::getBusinessId, businessId)
                        .or().isNull(SystemStoreInfoDO::getBusinessId)
                ));
    };

    /**
     * 查询老板助手组织范围或直接关联的门店，仅加载接口返回所需字段。
     *
     * @param orgIds 组织 ID 集合
     * @param storeIds 直接关联且可见的门店 ID 集合
     * @return 老板助手门店列表
     */
    default List<SystemStoreInfoDO> selectBossStoreList(Set<Long> orgIds, Set<Long> storeIds) {
        return selectBossStores(orgIds, storeIds, bossStoreSelectWrapper());
    }

    /** 精简列表复用原门店范围，只查询三个物理字段。 */
    default List<SystemStoreInfoDO> selectBossSimpleStoreList(Set<Long> orgIds, Set<Long> storeIds) {
        return selectBossStores(orgIds, storeIds, bossSimpleStoreSelectWrapper());
    }

    /** 保留原接口组织门店与直接关联门店的筛选语义。 */
    private List<SystemStoreInfoDO> selectBossStores(Set<Long> orgIds, Set<Long> storeIds,
                                                    LambdaQueryWrapperX<SystemStoreInfoDO> wrapper) {
        if ((orgIds == null || orgIds.isEmpty()) && (storeIds == null || storeIds.isEmpty())) {
            return List.of();
        }
        wrapper.eq(SystemStoreInfoDO::getStoreSource, 0)
                .eq(SystemStoreInfoDO::getDeleted, 0)
                .eq(SystemStoreInfoDO::getStoreStatus, 0)
                .inIfPresent(SystemStoreInfoDO::getOrgId, orgIds);
        if (orgIds != null && !orgIds.isEmpty() && storeIds != null && !storeIds.isEmpty()) {
            wrapper.or();
        }
        if (storeIds != null && !storeIds.isEmpty()) {
            wrapper.in(SystemStoreInfoDO::getStoreId, storeIds);
        }
        return selectList(wrapper);
    }

    /**
     * 查询老板助手当前用户的供应链门店，仅加载接口返回所需字段。
     *
     * @param userId 当前用户 ID
     * @param businessId 当前项目 ID
     * @return 供应链门店列表
     */
    @DataPermission(enable = false)
    default List<SystemStoreInfoDO> selectBossSupplyStoreList(Long userId, Long businessId) {
        return selectBossSupplyStores(userId, businessId, bossStoreSelectWrapper());
    }

    /** 供应链精简列表沿用原接口的数据权限及项目筛选规则。 */
    @DataPermission(enable = false)
    default List<SystemStoreInfoDO> selectBossSimpleSupplyStoreList(Long userId, Long businessId) {
        return selectBossSupplyStores(userId, businessId, bossSimpleStoreSelectWrapper());
    }

    private List<SystemStoreInfoDO> selectBossSupplyStores(Long userId, Long businessId,
                                                         LambdaQueryWrapperX<SystemStoreInfoDO> wrapper) {
        return selectList(wrapper
                .eq(SystemStoreInfoDO::getUserId, userId)
                .eq(SystemStoreInfoDO::getStoreStatus, 0)
                .eq(SystemStoreInfoDO::getDeleted, 0)
                .and(scope -> scope
                        .eq(SystemStoreInfoDO::getBusinessId, 11L)
                        .or().eq(SystemStoreInfoDO::getBusinessId, businessId)
                        .or().isNull(SystemStoreInfoDO::getBusinessId)));
    }

    /** 精简列表只读取ID、名称和使用状态。 */
    private static LambdaQueryWrapperX<SystemStoreInfoDO> bossSimpleStoreSelectWrapper() {
        LambdaQueryWrapperX<SystemStoreInfoDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.select(SystemStoreInfoDO::getStoreId, SystemStoreInfoDO::getStoreName,
                SystemStoreInfoDO::getUseStatus);
        return wrapper;
    }

    /**
     * 创建老板助手门店列表的字段投影，避免加载接口不会返回的门店字段。
     */
    private static LambdaQueryWrapperX<SystemStoreInfoDO> bossStoreSelectWrapper() {
        LambdaQueryWrapperX<SystemStoreInfoDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.select(
                SystemStoreInfoDO::getStoreId,
                SystemStoreInfoDO::getOrgId,
                SystemStoreInfoDO::getStoreName,
                SystemStoreInfoDO::getWarehouseId,
                SystemStoreInfoDO::getIdentificationTemplate,
                SystemStoreInfoDO::getTiktokId,
                SystemStoreInfoDO::getMeituanId,
                SystemStoreInfoDO::getHungryId,
                SystemStoreInfoDO::getStoreStatus,
                SystemStoreInfoDO::getOpenStatus,
                SystemStoreInfoDO::getStoreAddress,
                SystemStoreInfoDO::getStoreTakeaway,
                SystemStoreInfoDO::getCampusDeliveryStatus,
                SystemStoreInfoDO::getCampusDeliverySubsidy,
                SystemStoreInfoDO::getProjectCode,
                SystemStoreInfoDO::getIsSameLine,
                SystemStoreInfoDO::getWarehouseName,
                SystemStoreInfoDO::getProjectId,
                SystemStoreInfoDO::getProjectName,
                SystemStoreInfoDO::getDeliveryLineId,
                SystemStoreInfoDO::getDeliveryLineName,
                SystemStoreInfoDO::getStartBuyAmount,
                SystemStoreInfoDO::getAdvanceAmount,
                SystemStoreInfoDO::getBelieveAmount,
                SystemStoreInfoDO::getSupplyAddress,
                SystemStoreInfoDO::getUseStatus,
                SystemStoreInfoDO::getProjectOwnerShip,
                SystemStoreInfoDO::getIsOpenStatus,
                SystemStoreInfoDO::getStoreHoursExtra,
                SystemStoreInfoDO::getDeliveryTimeExtra,
                SystemStoreInfoDO::getPackageSetting,
                SystemStoreInfoDO::getDeliveryStartTime,
                SystemStoreInfoDO::getDeliveryEndTime,
                SystemStoreInfoDO::getIsOpenPassword,
                SystemStoreInfoDO::getIsAllProduct,
                SystemStoreInfoDO::getIsAllCommdity,
                SystemStoreInfoDO::getPeakHours,
                SystemStoreInfoDO::getMealTime,
                SystemStoreInfoDO::getIsPrompt,
                SystemStoreInfoDO::getPromptText);
        return wrapper;
    }

}
