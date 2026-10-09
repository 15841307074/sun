package com.htyoudao.youdao.module.promotion.service.activitySeckillTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillCommodity.ActivitySeckillCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillTime.ActivitySeckillTimeDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySeckillTime.ActivitySeckillTimeMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivitySeckillTimeServiceImpl implements ActivitySeckillTimeService {

    @Resource
    private ActivitySeckillTimeMapper activitySeckillTimeMapper;

    @Override
    public void createBatch(List<ActivitySeckillTimeDO> activitySeckillTimeDOS) {
        activitySeckillTimeMapper.insertBatch(activitySeckillTimeDOS);
    }

    @Override
    public void updateBatch(List<ActivitySeckillTimeDO> activitySeckillTimeDOS) {
        Long activityId = activitySeckillTimeDOS.get(0).getActivityId();
        List<ActivitySeckillTimeDO> insertList = new ArrayList<>();
        List<ActivitySeckillTimeDO> updateList = new ArrayList<>();
        List<Long> currentIds = new ArrayList<>(); // 记录传入列表中的所有ID
        for (ActivitySeckillTimeDO activitySeckillTimeDO : activitySeckillTimeDOS) {
            if (activitySeckillTimeDO.getId() == null) {
                insertList.add(activitySeckillTimeDO);
            }else {
                currentIds.add(activitySeckillTimeDO.getId());
                LambdaQueryWrapper<ActivitySeckillTimeDO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ActivitySeckillTimeDO::getId, activitySeckillTimeDO.getId());
                if (activitySeckillTimeMapper.exists(queryWrapper)) {
                    updateList.add(activitySeckillTimeDO);
                }else {
                    activitySeckillTimeDO.setId(null);
                    insertList.add(activitySeckillTimeDO);
                }
            }
        }
        // 处理删除逻辑
        if (!currentIds.isEmpty()) {
            // 查询数据库中存在但不在当前列表中的ID
            LambdaQueryWrapper<ActivitySeckillTimeDO> deleteWrapper = new LambdaQueryWrapper<>();

            deleteWrapper.notIn(ActivitySeckillTimeDO::getId, currentIds);

            deleteWrapper.eq(ActivitySeckillTimeDO::getActivityId, activityId);
            activitySeckillTimeMapper.delete(deleteWrapper);
        }
        if (!insertList.isEmpty()) {
            activitySeckillTimeMapper.insertBatch(insertList);
        }
        if (!updateList.isEmpty()) {
            activitySeckillTimeMapper.updateBatch(updateList);
        }
    }

    @Override
    public List<ActivitySeckillTimeDO> selectByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivitySeckillTimeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySeckillTimeDO::getActivityId, activityId);

        return activitySeckillTimeMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByActivityId(Long id) {
        LambdaQueryWrapper<ActivitySeckillTimeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySeckillTimeDO::getActivityId, id);
        activitySeckillTimeMapper.delete(queryWrapper);
    }
}
