package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.util.*;

@Service("lotteryRuntimeLifecycle")
@DS("master")
public class LotteryRuntimeLifecycleImpl implements LotteryRuntimeLifecycle {
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private LotterySettingsMapper settings;
    @Resource
    private LotteryCachePublisher publisher;
    @Resource
    private LotteryScopeService scope;
    @Resource
    private ActivityMapper activities;

    /** 各启用入口共用配置行锁和事务；非抽奖活动返回 false。 */
    @Transactional(rollbackFor=Exception.class)
    @Override
    public boolean changeState(long activityId, Integer state) {
        LotterySettingsDO cfg = settings.selectOne(new LambdaQueryWrapper<LotterySettingsDO>()
                .eq(LotterySettingsDO::getActivityId, activityId)
                .eq(LotterySettingsDO::getBusinessId, BusinessContextHolder.getRequiredBusinessId()).last("FOR UPDATE"));
        if (cfg == null) return false;
        if (!Objects.equals(state, 0) && !Objects.equals(state, 1))
            throw new IllegalArgumentException("启用状态只能为 0 或 1");
        var activity = activities.selectById(activityId);
        if (activity == null) throw new IllegalArgumentException("活动不存在");
        if (state == 1 && (!Objects.equals(cfg.getState(), 1)
                || !Objects.equals(activity.getIsEnabled(), 1))) initializeLegacy(cfg);
        cfg.setState(state);
        cfg.setConfigVersion(Optional.ofNullable(cfg.getConfigVersion()).orElse(0L) + 1);
        activity.setIsEnabled(state);
        settings.updateById(cfg);
        activities.updateById(activity);
        publisher.enqueue(cfg);
        return true;
    }

    /** 调用方持有配置行锁；不导入、清空或改写历史消耗。 */
    @Transactional(rollbackFor=Exception.class, propagation=Propagation.MANDATORY)
    @Override
    public void initializeLegacy(LotterySettingsDO cfg) {
        if (Objects.equals(cfg.getRuntimeVersion(), 2)) return;
        long b = BusinessContextHolder.getRequiredBusinessId();
        // 旧版标记若已存在新版业务数据，拒绝重新初始化。
        Long existing = jdbc.queryForObject("SELECT (SELECT COUNT(*) FROM lottery_v2_request WHERE business_id=? AND activity_id=?) + " +
                "(SELECT COUNT(*) FROM lottery_v2_counter WHERE business_id=? AND activity_id=?) + " +
                "(SELECT COUNT(*) FROM lottery_v2_activity_counter WHERE business_id=? AND activity_id=?) + " +
                "(SELECT COUNT(*) FROM lottery_v2_stock WHERE business_id=? AND activity_id=?)", Long.class,
                b,cfg.getActivityId(),b,cfg.getActivityId(),b,cfg.getActivityId(),b,cfg.getActivityId());
        if (existing != null && existing > 0) throw new IllegalArgumentException("历史活动存在新版运行数据，请核对运行版本，不能重复初始化");
        scope.prepareLegacyPrizes(cfg);
        cfg.setStockEpoch(Optional.ofNullable(cfg.getStockEpoch()).orElse(1L) + 1);
        cfg.setConfigVersion(Optional.ofNullable(cfg.getConfigVersion()).orElse(0L) + 1);
        cfg.setRuntimeVersion(2);
        // 重新启用开启新批次；新版次数按需从零创建，旧数据保留。
        seedStock(cfg);
        settings.updateById(cfg);
        publisher.enqueue(cfg);
    }
    private LotterySettingsDO lock(long a) {
        LotterySettingsDO cfg=settings.selectOne(new LambdaQueryWrapper<LotterySettingsDO>().eq(LotterySettingsDO::getActivityId,a).eq(LotterySettingsDO::getBusinessId,BusinessContextHolder.getRequiredBusinessId()).last("FOR UPDATE"));
        if(cfg==null)throw new IllegalArgumentException("活动不存在");return cfg;
    }
    @Transactional(rollbackFor=Exception.class)
    @Override
    public void reset(long a,String nextSession) {
        LotterySettingsDO cfg=lock(a);
        if(!Objects.equals(cfg.getRuntimeVersion(),2)||Objects.equals(cfg.getLastResetScope(),nextSession))return;
        cfg.setStockEpoch(cfg.getStockEpoch()+1);cfg.setConfigVersion(cfg.getConfigVersion()+1);cfg.setLastResetScope(nextSession);
        settings.updateById(cfg);seedStock(cfg);publisher.enqueue(cfg);
    }
    private void seedStock(LotterySettingsDO cfg) {
        // 包含暂时退出范围的门店，重新加入后仍能接续正常场次重置。
        jdbc.update("INSERT INTO lottery_v2_stock(business_id,activity_id,stock_epoch,pool_store_id,prize_code,prize_id,total,issued) " +
                "SELECT business_id,?,?,COALESCE(store_id,0),code,id,prize_num,0 FROM lottery_prize WHERE lottery_id=? AND business_id=? AND deleted=0 AND COALESCE(is_guarantees,0)<>1",
                cfg.getActivityId(),cfg.getStockEpoch(),cfg.getId(),BusinessContextHolder.getRequiredBusinessId());
    }
    @Override
    public void rejectLegacyWrite(Long id) {
        if(id==null)return;
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM lottery_settings WHERE business_id=? AND (id=? OR activity_id=?) AND runtime_version=2",Integer.class,BusinessContextHolder.getRequiredBusinessId(),id,id);
        if(count!=null&&count>0)throw new IllegalArgumentException("该活动已切换 V2，请使用新版活动管理接口");
    }
}
