package com.htyoudao.youdao.module.promotion.service.activityStrore;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStore.ActivityStoreMapper;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author villky
 */
@Service
@Slf4j
@RefreshScope
public class ActivityStoreServiceImpl extends ServiceImpl<ActivityStoreMapper, ActivityStoreDO> implements
    ActivityStoreService {

    @Resource
    private ActivityStoreMapper activityStoreMapper;

    @Resource
    private StoreApi storeApi;


    @Override
    public void createBatch(List<Long> storeIds, Long activityId) {

        //去重门店ID集合
        storeIds = storeIds.stream().distinct().toList();

        List<ActivityStoreDO> activityStoreDOS = new ArrayList<>();
        for (Long storeId : storeIds) {
            ActivityStoreDO activityStoreDO = new ActivityStoreDO();
            activityStoreDO.setActivityId(activityId);
            activityStoreDO.setStoreId(storeId);
            activityStoreDOS.add(activityStoreDO);
        }
        this.saveBatch(activityStoreDOS);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityStoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityStoreDO::getActivityId, activityId);
        activityStoreMapper.delete(queryWrapper);
    }

    @Override
    public List<ActivityStoreDO> selectByActivityId(Long id) {
        LambdaQueryWrapper<ActivityStoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityStoreDO::getActivityId, id);
        return activityStoreMapper.selectList(queryWrapper);
    }

    @Override
    public List<StoreInfoDTO> storesByActivityId(Long id) {
        List<ActivityStoreDO> storeDOS = this.selectByActivityId(id);
        if (CollectionUtils.isEmpty(storeDOS)) {
            return List.of();
        }

        List<Long> storeIds = storeDOS.stream().map(ActivityStoreDO::getStoreId).collect(Collectors.toList());
        CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(storeIds);
        return storesByStoreIds.getData();
    }
    @Override
    public List<Long> selectStoreIdsByActivityId(Long id) {
        List<ActivityStoreDO> storeDOS = this.selectByActivityId(id);
        if (CollectionUtils.isEmpty(storeDOS)) {
            return List.of();
        }
        return storeDOS.stream().map(ActivityStoreDO::getStoreId).collect(Collectors.toList());
    }
}
