package com.htyoudao.youdao.module.system.service.wxstore.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.system.controller.admin.wxstore.vo.*;
import com.htyoudao.youdao.module.system.controller.app.wxstore.vo.AppWeChatStoreImgRespVO;
import com.htyoudao.youdao.module.system.controller.app.wxstore.vo.AppWeChatStoreRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storeuser.StoreUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.tagvalue.TagValueDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.wxstore.StoreWecomConfigDO;
import com.htyoudao.youdao.module.system.dal.dataobject.wxstore.StoreWecomImgDO;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.storeuser.StoreUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.user.AdminUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.wxstore.StoreWecomConfigMapper;
import com.htyoudao.youdao.module.system.dal.mysql.wxstore.StoreWecomImgMapper;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.couponpackage.CouponPackageApi;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.GoodCouponApi;
import com.htyoudao.youdao.module.system.service.orguser.OrgUserService;
import com.htyoudao.youdao.module.system.service.store.cache.StoreCityListCacheService;
import com.htyoudao.youdao.module.system.service.wxstore.StoreWecomConfigService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

@Slf4j
@Service
public class StoreWecomConfigServiceImpl implements StoreWecomConfigService {

    @Resource
    private StoreWecomConfigMapper storeWecomConfigMapper;
    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;
    @Resource
    private OrgMapper orgMapper;
    ;
    @Resource
    private StoreUserMapper storeUserMapper;
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private OrgUserService orgUserService;
    @Resource
    private StoreWecomImgMapper storeWecomImgMapper;
    @Resource
    private StoreCityListCacheService storeCityListCacheService;
    @DubboReference
    private ActivityApi activityApi;
    @DubboReference
    private GoodCouponApi goodCouponApi;
    @DubboReference
    private CouponPackageApi couponPackageApi;

    @Override
    public StoreWecomConfigRespVO selectStoreWecomConfigById(Long id) {
        StoreWecomConfigRespVO storeWecomConfigRespVO = new StoreWecomConfigRespVO();
        LambdaQueryWrapper<StoreWecomConfigDO> storeWecomConfigDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        storeWecomConfigDOLambdaQueryWrapper.eq(StoreWecomConfigDO::getStoreId, id)
                // 使用 or() 表示两个条件满足其一即可
                .and(wrapper -> wrapper.isNotNull(StoreWecomConfigDO::getQrCode)
                        .or()
                        .isNotNull(StoreWecomConfigDO::getQrCommunityCode));

        List<StoreWecomConfigDO> configList = storeWecomConfigMapper.selectList(storeWecomConfigDOLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(configList)) {
            return storeWecomConfigRespVO;
        }
        StoreWecomConfigDO storeWecomConfigDO = configList.get(0);
        BeanUtils.copyProperties(storeWecomConfigDO, storeWecomConfigRespVO);
        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(id);
        if (systemStoreInfoDO != null) {
            Set<Long> userIds = new HashSet<>();
            String storeLeader = Optional.ofNullable(systemStoreInfoDO.getStoreLeader()).orElse("");
            String storeLeaderPhone = Optional.ofNullable(systemStoreInfoDO.getStoreLeaderPhone()).orElse("");
            String result = storeLeader;
            if (!storeLeaderPhone.isEmpty()) {
                result += " / " + storeLeaderPhone;
            }
            storeWecomConfigRespVO.setStoreLeader(result);
            OrgDO orgDO = orgMapper.selectById(systemStoreInfoDO.getOrgId());
            storeWecomConfigRespVO.setOrgName(orgDO.getName());
            List<StoreUserDO> userDOList = storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>()
                    .eqIfPresent(StoreUserDO::getStoreId, id)
                    .eq(StoreUserDO::getType, 1)
            );
            if (userDOList != null && !CollectionUtils.isEmpty(userDOList)) {
                userDOList.forEach(item1 -> {
                    userIds.add(item1.getUserId());
                });
            }
            Map<Long, AdminUserDO> userMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(userIds)) {
                userMap = adminUserMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(AdminUserDO::getId, user -> user));
            }
            if (!CollectionUtils.isEmpty(userMap) && userDOList != null && !CollectionUtils.isEmpty(userDOList)) {
                StringBuilder regionUsers = new StringBuilder();
                for (StoreUserDO userDO : userDOList) {
                    AdminUserDO adminUserDO = userMap.get(userDO.getUserId());
                    if (adminUserDO != null) {
                        if (regionUsers.length() > 0) {
                            regionUsers.append(", ");
                        }
                        regionUsers.append(adminUserDO.getNickname()).append("/").append(adminUserDO.getMobile());
                    }
                }
                storeWecomConfigRespVO.setRegionUser(regionUsers.toString());
            }
        }
        return storeWecomConfigRespVO;
    }

    @Override
    @LogRecord(type = SYSTEM_WX_STORE_CONFIG_TYPE, subType = SYSTEM_WX_STORE_CONFIG_ADD_TYPE, bizNo = "{{#storeWecomConfig.id}}", success = SYSTEM_WX_STORE_CONFIG_ADD_SUCCESS)
    public Integer insertStoreWecomConfig(StoreWecomConfigReqVO storeWecomConfigReqVO) {
        LambdaQueryWrapper<StoreWecomConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoreWecomConfigDO::getStoreId, storeWecomConfigReqVO.getStoreId());

        List<StoreWecomConfigDO> existingConfigs = storeWecomConfigMapper.selectList(queryWrapper);

        if (existingConfigs != null && existingConfigs.size() > 0) {
            // 如果存在多条记录，先删除多余的重复数据
            if (existingConfigs.size() > 1) {
                // 保留第一条，删除其他重复记录
                StoreWecomConfigDO keepConfig = existingConfigs.get(0);
                List<Long> deleteIds = existingConfigs.stream()
                        .skip(1) // 跳过要保留的第一条
                        .map(StoreWecomConfigDO::getId)
                        .collect(Collectors.toList());

                // 批量删除多余的重复记录
                storeWecomConfigMapper.deleteBatchIds(deleteIds);
            }

            // 更新保留的那条记录
            StoreWecomConfigDO updateConfig = existingConfigs.get(0);
            updateConfig.setQrCode(storeWecomConfigReqVO.getQrCode());
            updateConfig.setQrType(storeWecomConfigReqVO.getQrType());
            updateConfig.setQrCommunityCode(storeWecomConfigReqVO.getQrCommunityCode());

            int result = storeWecomConfigMapper.updateById(updateConfig);
            LogRecordContext.putVariable("storeWecomConfig", updateConfig);
            LogRecordContext.putVariable("storeWecomConfig", storeWecomConfigReqVO);
            storeCityListCacheService.refreshStoresAfterCommit(
                    Collections.singletonList(storeWecomConfigReqVO.getStoreId()));
            return result;
        } else {
            // 不存在则插入新记录
            StoreWecomConfigDO newConfig = BeanUtils.toBean(storeWecomConfigReqVO, StoreWecomConfigDO.class);
            int result = storeWecomConfigMapper.insert(newConfig);
            LogRecordContext.putVariable("storeWecomConfig", newConfig);
            LogRecordContext.putVariable("storeWecomConfig", storeWecomConfigReqVO);
            storeCityListCacheService.refreshStoresAfterCommit(
                    Collections.singletonList(storeWecomConfigReqVO.getStoreId()));
            return result;
        }
        // 记录操作日志上下文

    }

    @Override
    public Integer communityImgAdd(StoreWecomImgReqVO storeWecomImgReqVO) {
        List<StoreWecomImgDO> configList = storeWecomImgMapper.selectList();
        if (configList != null && !configList.isEmpty()) {
            StoreWecomImgDO storeWecomImgDO = configList.get(0);
            storeWecomImgDO.setCommunityManagerImg(storeWecomImgReqVO.getCommunityManagerImg());
            storeWecomImgDO.setCommunityStoreImg(storeWecomImgReqVO.getCommunityStoreImg());
            int result = storeWecomImgMapper.updateById(storeWecomImgDO);
            return result;
        } else {
            StoreWecomImgDO storeWecomImgDO = new StoreWecomImgDO();
            storeWecomImgDO.setCommunityManagerImg(storeWecomImgReqVO.getCommunityManagerImg());
            storeWecomImgDO.setCommunityStoreImg(storeWecomImgReqVO.getCommunityStoreImg());
            int result = storeWecomImgMapper.insert(storeWecomImgDO);
            return result;
        }
    }

    @Override
    public PageResult<StoreWecomConfigRespVO> getStoreWecomPage(StoreWecomPageReqVO pageReqVO) {
        //查询deptIDs
        Long businessId = BusinessContextHolder.getBusinessId();
        if (businessId.equals(1L)) {
            businessId = null;
        }
        Set<Long> orgIds = new HashSet<>();
        Long userId = WebFrameworkUtils.getLoginUserId();
        //当前用户的机构ID
        List<Long> orgId = orgUserService.selectUserOrgIds(userId);
        orgIds.addAll(orgId);
        if (pageReqVO.getOrgId() != null) {
            orgIds = orgMapper.getChildIdListByStore(pageReqVO.getOrgId());
            if (orgIds.isEmpty()) {
                Set<Long> businessIdList = new HashSet<>();
                businessIdList.add(pageReqVO.getOrgId());
                List<OrgDO> orgDOList = orgMapper.selectList(businessIdList);
                orgIds.addAll(orgDOList.stream().map(OrgDO::getId).collect(Collectors.toSet()));
            }
            orgIds.add(pageReqVO.getOrgId());
            pageReqVO.setOrgIds(orgIds);
        } else {
            Set<Long> finalOrgIds = new HashSet<>();
            orgId.forEach(orgd -> {
                finalOrgIds.addAll(orgMapper.getChildIdListByStore(orgd).stream().collect(Collectors.toSet()));
            });
            orgIds = orgMapper.getChildIdListByStore(pageReqVO.getOrgId());
            Set<Long> businessIdList = new HashSet<>();
            businessIdList.add(pageReqVO.getOrgId());
            List<OrgDO> orgDOList = orgMapper.selectList(businessIdList);
            if (!CollectionUtils.isEmpty(orgDOList)) {
                orgIds.addAll(orgDOList.stream().map(OrgDO::getId).collect(Collectors.toSet()));
            }
            if (pageReqVO.getOrgId() != null) {
                orgIds.add(pageReqVO.getOrgId());
            }
            if (!CollectionUtils.isEmpty(finalOrgIds)) {
                orgIds.addAll(finalOrgIds);
            }
            pageReqVO.setOrgIds(orgIds);
        }
        pageReqVO.setOrgIds(orgIds);
        Page<StoreWecomConfigRespVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        IPage<StoreWecomConfigRespVO> pageList = systemStoreInfoMapper.selectWecomPageList(page, pageReqVO, businessId, userId);
        PageResult<StoreWecomConfigRespVO> pageResult = new PageResult<>(pageList.getRecords(), pageList.getTotal());
        return joinData(pageResult);
    }

    @Override
    @LogRecord(type = SYSTEM_WX_STORE_CONFIG_TYPE, subType = SYSTEM_WX_STORE_CONFIG_DELETE_TYPE, bizNo = "{{#storeWecomConfigId}}", success = SYSTEM_WX_STORE_CONFIG_DELETE_SUCCESS)
    public Integer deleteStoreWecomConfig(Long id) {
        LambdaQueryWrapper<StoreWecomConfigDO> storeWecomConfigDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        storeWecomConfigDOLambdaQueryWrapper.eq(StoreWecomConfigDO::getStoreId, id);
        List<StoreWecomConfigDO> storeWecomConfigs = storeWecomConfigMapper.selectList(storeWecomConfigDOLambdaQueryWrapper);
        if (!storeWecomConfigs.isEmpty()) {
            StoreWecomConfigDO storeWecomConfig = storeWecomConfigs.get(0);
            storeWecomConfig.setQrCode("");
            storeWecomConfig.setQrCommunityCode("");
            int result = storeWecomConfigMapper.updateById(storeWecomConfig);
            // 记录操作日志上下文
            LogRecordContext.putVariable("storeWecomConfigId", id);
            storeCityListCacheService.refreshStoresAfterCommit(Collections.singletonList(id));
            return result;
        }
        // 记录操作日志上下文
        LogRecordContext.putVariable("storeWecomConfigId", id);
        return 0;
    }

    private PageResult<StoreWecomConfigRespVO> joinData(PageResult<StoreWecomConfigRespVO> pageResult) {
        if (pageResult.getList() == null || CollectionUtils.isEmpty(pageResult.getList())) {
            return pageResult;
        }
        // 收集需要查询的ID
        Set<Long> orgIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        Set<Long> tagIds = new HashSet<>();
        for (StoreWecomConfigRespVO item : pageResult.getList()) {
            if (item.getOrgId() != null) {
                orgIds.add(item.getOrgId());
            }
            List<StoreUserDO> userDOList = storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>()
                    .eqIfPresent(StoreUserDO::getStoreId, item.getStoreId())
                    .eq(StoreUserDO::getType, 1)
            );
            if (userDOList != null && !CollectionUtils.isEmpty(userDOList)) {
                userDOList.forEach(item1 -> {
                    userIds.add(item1.getUserId());
                });
            }
        }
        Map<Long, OrgDO> orgMap = new HashMap<>();
        Map<Long, AdminUserDO> userMap = new HashMap<>();
        Map<Long, TagValueDO> tagMap = new HashMap<>();
        // 批量查询
        if (!CollectionUtils.isEmpty(orgIds)) {
            orgMap = orgMapper.selectBatchIds(orgIds).stream()
                    .collect(Collectors.toMap(OrgDO::getId, org -> org));
        }
        if (!CollectionUtils.isEmpty(userIds)) {
            userMap = adminUserMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(AdminUserDO::getId, user -> user));
        }
        // 填充数据
        for (StoreWecomConfigRespVO item : pageResult.getList()) {
            OrgDO org = orgMap.get(item.getOrgId());
            if (org != null) {
                item.setOrgName(org.getName());
            }
            List<StoreUserDO> userDOList = storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>()
                    .eqIfPresent(StoreUserDO::getStoreId, item.getStoreId())
                    .eq(StoreUserDO::getType, 1)
            );
            if (!CollectionUtils.isEmpty(userMap) && userDOList != null && !CollectionUtils.isEmpty(userDOList)) {
                StringBuilder regionUsers = new StringBuilder();
                for (StoreUserDO userDO : userDOList) {
                    AdminUserDO adminUserDO = userMap.get(userDO.getUserId());
                    if (adminUserDO != null) {
                        if (regionUsers.length() > 0) {
                            regionUsers.append(", ");
                        }
                        regionUsers.append(adminUserDO.getNickname()).append("/").append(adminUserDO.getMobile());
                    }
                }
                item.setRegionUser(regionUsers.toString());
            }
            String storeLeader = Optional.ofNullable(item.getStoreLeader()).orElse("");
            String storeLeaderPhone = Optional.ofNullable(item.getStoreLeaderPhone()).orElse("");
            String result = storeLeader;
            if (!storeLeaderPhone.isEmpty()) {
                result += " / " + storeLeaderPhone;
            }

            item.setStoreLeader(result);
        }
        return pageResult;
    }

    @Override
    public AppWeChatStoreRespVO getQrCodeByStoreId(Long storeId) {
        QueryWrapper<StoreWecomConfigDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StoreWecomConfigDO::getStoreId, storeId);
        queryWrapper.last("limit 1");
        StoreWecomConfigDO storeWecomConfigDO = storeWecomConfigMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(storeWecomConfigDO) || ObjectUtil.isEmpty(storeWecomConfigDO.getId())) {
            return new AppWeChatStoreRespVO();
        }
        AppWeChatStoreRespVO bean = BeanUtils.toBean(storeWecomConfigDO, AppWeChatStoreRespVO.class);
        Integer qrType = storeWecomConfigDO.getQrType();
        if (qrType == 1) {
            //社群二维码
            bean.setQrCode(storeWecomConfigDO.getQrCommunityCode());
        } else {
            // 企微二维码
            bean.setQrCode(storeWecomConfigDO.getQrCode());
        }
        return bean;
    }

    @Override
    public void updateCodeType(StoresCodeUpdateVO storeUpdateVO) {
        if (storeUpdateVO == null) {
            throw new IllegalArgumentException("入参不能为空");
        }
        List<Long> storeIds = storeUpdateVO.getStoreIds();
        if (CollectionUtils.isEmpty(storeIds)) {
            throw new IllegalArgumentException("门店ID列表不能为空");
        }
        if (CollectionUtils.isEmpty(storeIds)) {
            throw new IllegalArgumentException("门店ID列表不能为空");
        }

        LambdaUpdateWrapper<StoreWecomConfigDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(StoreWecomConfigDO::getStoreId, storeIds);
        updateWrapper.set(StoreWecomConfigDO::getQrType, storeUpdateVO.getCodeType());

        storeWecomConfigMapper.update(null, updateWrapper);
        storeCityListCacheService.refreshStoresAfterCommit(storeIds);
    }

    @Override
    public AppWeChatStoreImgRespVO getQrCodeBgByStoreId(Long storeId, Long couponId, Long activityId, Long packageId) {
        AppWeChatStoreImgRespVO appWeChatStoreImgRespVO = new AppWeChatStoreImgRespVO();
        if (storeId == null) {
            return appWeChatStoreImgRespVO;
        }
        List<StoreWecomConfigDO> configList = storeWecomConfigMapper.selectList(
                new QueryWrapper<StoreWecomConfigDO>().lambda()
                        .eq(StoreWecomConfigDO::getStoreId, storeId)
        );
        if (CollectionUtils.isEmpty(configList)) {
            return appWeChatStoreImgRespVO;
        }
        StoreWecomConfigDO configDO = configList.get(0);

        List<String> imgUrls = this.getBusinessQrImages(activityId, couponId, packageId);
        String businessImg = this.selectQrImage(configDO.getQrType(), imgUrls);
        if (ObjectUtil.isNotEmpty(businessImg)) {
            appWeChatStoreImgRespVO.setCommunityImg(businessImg);
            return appWeChatStoreImgRespVO;
        }

        List<StoreWecomImgDO> imgList = storeWecomImgMapper.selectList();
        if (CollectionUtils.isEmpty(imgList)) {
            return appWeChatStoreImgRespVO;
        }
        StoreWecomImgDO imgDO = imgList.get(0);
        appWeChatStoreImgRespVO.setCommunityImg(configDO.getQrType() == 1
                ? imgDO.getCommunityStoreImg()
                : imgDO.getCommunityManagerImg());
        return appWeChatStoreImgRespVO;
    }

    private List<String> getBusinessQrImages(Long activityId, Long couponId, Long packageId) {
        if (activityId != null) {
            List<String> activityImages = activityApi.getGuideImage(activityId);
            if (!CollectionUtils.isEmpty(activityImages)) {
                return activityImages;
            }
        }
        if (couponId != null) {
            List<String> couponImages = goodCouponApi.getCommunityQrImage(couponId);
            if (!CollectionUtils.isEmpty(couponImages)) {
                return couponImages;
            }
        }
        if (packageId != null) {
            List<String> packageImages = couponPackageApi.getCommunityQrImage(packageId);
            if (!CollectionUtils.isEmpty(packageImages)) {
                return packageImages;
            }
        }
        return List.of();
    }

    private String selectQrImage(Integer qrType, List<String> imgUrls) {
        if (CollectionUtils.isEmpty(imgUrls)) {
            return null;
        }
        int index = Objects.equals(qrType, 1) ? 1 : 0;
        if (index >= imgUrls.size()) {
            return null;
        }
        return imgUrls.get(index);
    }

    @Override
    public StoreWecomImgDO communityImgDetail() {
        List<StoreWecomImgDO> imgList = storeWecomImgMapper.selectList();
        if (!CollectionUtils.isEmpty(imgList)) {
            return imgList.get(0);
        }
        return new StoreWecomImgDO();
    }
}
