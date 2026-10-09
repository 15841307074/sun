package com.htyoudao.youdao.module.system.service.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreStatusLogDO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreStatusLogMapper;
import com.htyoudao.youdao.module.system.service.store.cache.StoreCityListCacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RefreshScope
public class JobServiceImpl implements JobService {

    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;
    @Resource
    private SystemStoreStatusLogMapper systemStoreStatusLogMapper;
    @Resource
    private StoreCityListCacheService storeCityListCacheService;

    @Override
    @DataPermission(enable = false)
    public void storeStatusJob() {
        List<SystemStoreInfoDO> systemStoreInfoDOList = systemStoreInfoMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreInfoDO>().eq(SystemStoreInfoDO::getStoreStatus, 2)
                        .eq(SystemStoreInfoDO::getDeleted, 0)
        );
        List<Long> storeIdList = systemStoreInfoDOList.stream().map(SystemStoreInfoDO::getStoreId).toList();
        if (!CollectionUtils.isEmpty(storeIdList)) {
            LocalDateTime sevenDaysAgo = LocalDateTime.now()
                    .minusDays(7)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            LambdaQueryWrapper<SystemStoreStatusLogDO> queryWrapper = new LambdaQueryWrapper<SystemStoreStatusLogDO>()
                    .in(SystemStoreStatusLogDO::getStoreId, storeIdList)
                    .eq(SystemStoreStatusLogDO::getDeleted, 0)
                    .eq(SystemStoreStatusLogDO::getStoreStatus, 2)
                    .le(SystemStoreStatusLogDO::getCreateTime, sevenDaysAgo)
                    // 关键修改：子查询表起别名s，关联主表的store_id，实现「每个门店的最新记录」
                    .apply("create_time = (SELECT MAX(s.create_time) FROM system_store_status_log s WHERE s.store_id = system_store_status_log.store_id AND s.deleted = 0)")
                    // 关键修改：子查询表起别名s，精准关联门店，确保该门店没有比sevenDaysAgo更新的状态2记录
                    .apply("NOT EXISTS (SELECT 1 FROM system_store_status_log s WHERE s.store_id = system_store_status_log.store_id AND s.deleted = 0 AND s.store_status = 2 AND s.create_time > {0})", sevenDaysAgo);
            List<SystemStoreStatusLogDO> systemStoreStatusLogDOList = systemStoreStatusLogMapper.selectList(queryWrapper);
            if (!CollectionUtils.isEmpty(systemStoreStatusLogDOList)) {
                List<Long> storeIds = systemStoreStatusLogDOList.stream().map(SystemStoreStatusLogDO::getStoreId).toList();
                systemStoreInfoMapper.update(new SystemStoreInfoDO().setStoreStatus(1),
                        new LambdaQueryWrapper<SystemStoreInfoDO>().in(SystemStoreInfoDO::getStoreId, storeIds));

                Set<Long> updatedStoreIds = new HashSet<>(storeIds);
                Map<Long, String> storeCityMap = systemStoreInfoDOList.stream()
                        .filter(store -> updatedStoreIds.contains(store.getStoreId()))
                        .collect(Collectors.toMap(SystemStoreInfoDO::getStoreId,
                                store -> store.getStoreCity() == null ? "" : store.getStoreCity(),
                                (left, right) -> left));
                storeCityListCacheService.removeStoresAfterCommit(storeCityMap);

                for(Long storeId:storeIds){
                    SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(storeId);
                    SystemStoreStatusLogDO systemStoreStatusLogDO = new SystemStoreStatusLogDO();
                    systemStoreStatusLogDO.setStoreId(storeId);
                    systemStoreStatusLogDO.setCreator(systemStoreInfoDO.getCreator());
                    systemStoreStatusLogDO.setUpdater(systemStoreInfoDO.getUpdater());
                    systemStoreStatusLogDO.setBusinessId(systemStoreInfoDO.getBusinessId());
                    systemStoreStatusLogDO.setStoreStatus(1);
                    systemStoreStatusLogMapper.insert(systemStoreStatusLogDO);
                }
            }

        }

    }
}
