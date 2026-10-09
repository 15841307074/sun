package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryPrizeReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStore.ActivityStoreMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStoreTag.ActivityStoreTagDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.*;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStoreTag.ActivityStoreTagMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.*;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Service("lotteryScopeService")
@DS("master")
public class LotteryScopeServiceImpl implements LotteryScopeService {
    @Resource
    private ActivityStoreService stores;
    @Resource
    private ActivityStoreTagMapper tags;
    @Resource
    private LotterySettingsMapper settingsMapper;
    @Resource
    private LotteryPrizeMapper prizes;
    @Resource
    private ObjectMapper json;
    @Resource
    private LotteryCachePublisher publisher;
    @Resource
    private ActivityMapper activities;
    @Resource
    private ActivityStoreMapper activityStores;
    @Resource
    private TransactionTemplate transactions;
    @DubboReference(timeout = 3000, retries = 0) private StoreApi storeApi;

    @Override
    public List<Long> tagIds(long activityId) {
        return tags.selectList(new LambdaQueryWrapper<ActivityStoreTagDO>()
                .eq(ActivityStoreTagDO::getActivityId, activityId)).stream()
                .map(ActivityStoreTagDO::getTagId).distinct().sorted().toList();
    }

    /** 管理端事务内重建参与关系，不改写历史库存。 */
    @Override
    public List<Long> replace(ActivityDO activity, List<Long> tagIds, List<Long> storeIds) {
        int scope = activity.getAppScope() == null ? 0 : activity.getAppScope();
        if (scope != 0 && scope != 1) throw new IllegalArgumentException("appScope 只能为 0 或 1");
        boolean allStores = scope == 0 && Objects.equals(activity.getActivityStore(), 1);
        List<Long> normalizedTags = normalize(tagIds);
        List<Long> targets;
        if (scope == 1) {
            if (normalizedTags.isEmpty()) throw new IllegalArgumentException("按标签时至少选择一个标签");
            activity.setActivityStore(0);
            targets = storeApi.getStoreIdsByTagIds(normalizedTags).values().stream()
                    .flatMap(Collection::stream).map(StoreInfoDTO::getStoreId).distinct().sorted().toList();
        } else if (allStores) {
            targets = storeApi.getAllStoreList().getCheckedData().stream()
                    .map(StoreInfoDTO::getStoreId).distinct().sorted().toList();
        } else {
            targets = normalize(storeIds);
        }
        tags.delete(new LambdaQueryWrapper<ActivityStoreTagDO>().eq(ActivityStoreTagDO::getActivityId, activity.getId()));
        if (scope == 1) for (Long tagId : normalizedTags) {
            ActivityStoreTagDO row = new ActivityStoreTagDO();
            row.setActivityId(activity.getId()); row.setTagId(tagId); tags.insert(row);
        }
        // 管理端保存量很低；提交前核对关系，避免页面显示保存成功却没有标签可回显。
        if (scope == 1 && !tagIds(activity.getId()).equals(normalizedTags))
            throw new IllegalStateException("抽奖适用标签保存失败");
        stores.deleteByActivityId(activity.getId());
        // 全部门店沿用原有空关系表示法，避免后续新门店被已有关系意外排除。
        if (!allStores && !targets.isEmpty()) stores.createBatch(targets, activity.getId());
        return targets;
    }

    @Override
    public List<LotteryPrizeReqVO> template(LotterySettingsDO settings) {
        if (settings.getPrizeTemplateJson() == null) return List.of();
        try { return json.readValue(settings.getPrizeTemplateJson(), new TypeReference<>() {}); }
        catch (Exception e) { throw new IllegalStateException("奖品模板损坏", e); }
    }

    /** 保留旧奖品和已用数量，只补齐缺失的稳定编码与模板。 */
    @Override
    public void prepareLegacyPrizes(LotterySettingsDO settings) {
        if (settings.getPrizePoolRules() == null) settings.setPrizePoolRules(1);
        List<LotteryPrizeDO> existing = prizes.selectList(new LambdaQueryWrapper<LotteryPrizeDO>()
                .eq(LotteryPrizeDO::getLotteryId, settings.getId()).orderByAsc(LotteryPrizeDO::getId));
        if (existing.isEmpty()) {
            List<LotteryPrizeReqVO> saved = template(settings);
            if (saved.isEmpty()) throw new IllegalArgumentException("旧活动缺少奖品配置，请先保存奖品后再启用");
            syncPrizes(settings, saved, stores.selectStoreIdsByActivityId(settings.getActivityId()), null);
            return;
        }
        Map<Long, List<LotteryPrizeDO>> pools = new LinkedHashMap<>();
        for (LotteryPrizeDO prize : existing) {
            long pool = prize.getStoreId() == null ? 0 : prize.getStoreId();
            if ((Objects.equals(settings.getPrizePoolRules(), 2) && pool <= 0)
                    || (!Objects.equals(settings.getPrizePoolRules(), 2) && pool != 0))
                throw new IllegalArgumentException("旧活动奖池与门店配置不一致，请先修正奖品配置");
            pools.computeIfAbsent(pool, ignored -> new ArrayList<>()).add(prize);
        }
        for (List<LotteryPrizeDO> pool : pools.values()) {
            if (pool.stream().filter(p -> Objects.equals(p.getIsGuarantees(), 1)).count() != 1)
                throw new IllegalArgumentException("旧活动每个奖池必须配置唯一保底奖品");
            Set<String> codes = new HashSet<>();
            for (LotteryPrizeDO prize : pool) {
                if (prize.getPrizeNum() == null || prize.getPrizeNum() < 0)
                    throw new IllegalArgumentException("旧活动奖品数量不合法");
                if (prize.getCode() == null || prize.getCode().isBlank()) {
                    try {
                        LotteryPrizeReqVO identity = legacyTemplateItem(prize);
                        identity.setLotteryId(null); identity.setCode(null);
                        prize.setCode("legacy_" + UUID.nameUUIDFromBytes(json.writeValueAsBytes(identity)).toString().replace("-", ""));
                    } catch (Exception e) { throw new IllegalStateException("旧奖品身份初始化失败", e); }
                    LotteryPrizeDO patch = new LotteryPrizeDO(); patch.setId(prize.getId()); patch.setCode(prize.getCode());
                    prizes.updateById(patch);
                }
                if (!prize.getCode().matches("[A-Za-z0-9_-]{1,64}") || !codes.add(prize.getCode()))
                    throw new IllegalArgumentException("旧活动奖品 code 不合法或重复，请先修正奖品配置");
            }
        }
        if (settings.getPrizeTemplateJson() == null || settings.getPrizeTemplateJson().isBlank()) {
            List<LotteryPrizeReqVO> saved = new ArrayList<>();
            for (LotteryPrizeDO prize : pools.values().iterator().next()) {
                saved.add(legacyTemplateItem(prize));
            }
            try { settings.setPrizeTemplateJson(json.writeValueAsString(saved)); }
            catch (Exception e) { throw new IllegalStateException("奖品模板生成失败", e); }
        }
    }

    private LotteryPrizeReqVO legacyTemplateItem(LotteryPrizeDO prize) {
        LotteryPrizeReqVO item = BeanUtils.toBean(prize, LotteryPrizeReqVO.class);
        item.setId(null); item.setRemainNum(null);
        item.setWinningCitys(prize.getWinningCitys() == null || prize.getWinningCitys().isBlank()
                ? List.of() : Arrays.asList(prize.getWinningCitys().split(",")));
        return item;
    }

    @Override
    public void syncPrizes(LotterySettingsDO settings, List<LotteryPrizeReqVO> template, List<Long> targets,
                           Integer previousPool) {
        if (template == null || template.stream().filter(p -> Objects.equals(p.getIsGuarantees(), 1)).count() != 1)
            throw new IllegalArgumentException("必须配置唯一保底奖品");
        Set<String> codes = new HashSet<>();
        for (LotteryPrizeReqVO prize : template) {
            if (prize.getIsGuarantees() == null) prize.setIsGuarantees(0);
            if (!Objects.equals(prize.getIsGuarantees(),0) && !Objects.equals(prize.getIsGuarantees(),1))
                throw new IllegalArgumentException("isGuarantees 只能为 0 或 1");
            if (prize.getCode() == null || prize.getCode().isBlank()) prize.setCode(UUID.randomUUID().toString().replace("-", ""));
            if (!prize.getCode().matches("[A-Za-z0-9_-]{1,64}") || !codes.add(prize.getCode()))
                throw new IllegalArgumentException("奖品 code 不合法或重复");
            if (prize.getPrizeNum() == null || prize.getPrizeNum() < 0) throw new IllegalArgumentException("奖品数量不能为负数");
        }
        List<LotteryPrizeDO> existing = prizes.selectList(new LambdaQueryWrapper<LotteryPrizeDO>()
                .eq(LotteryPrizeDO::getLotteryId, settings.getId()).orderByAsc(LotteryPrizeDO::getId));
        if (previousPool != null && !Objects.equals(previousPool, settings.getPrizePoolRules())) {
            publisher.assertNoPending(settings.getActivityId());
            prizes.delete(new LambdaQueryWrapper<LotteryPrizeDO>().eq(LotteryPrizeDO::getLotteryId, settings.getId()));
            existing = List.of();
            settings.setStockEpoch(Optional.ofNullable(settings.getStockEpoch()).orElse(1L) + 1);
        }
        Map<String, LotteryPrizeReqVO> byCode = template.stream().collect(Collectors.toMap(LotteryPrizeReqVO::getCode, p -> p));
        Map<String, LotteryPrizeDO> byPool = new HashMap<>();
        for (LotteryPrizeDO old : existing) {
            LotteryPrizeReqVO item = byCode.get(old.getCode());
            if (item == null) {
                publisher.assertPrizeNoPending(settings.getActivityId(), old.getCode());
                prizes.deleteById(old.getId());
                continue;
            }
            int used = Optional.ofNullable(old.getRemainNum()).orElse(0);
            if (!Objects.equals(settings.getRuntimeVersion(), 2) && item.getPrizeNum() < used) throw new IllegalArgumentException("奖品总量不能小于已发放及预占数量");
            publisher.resizeStock(settings, old, item.getPrizeNum());
            LotteryPrizeDO update = toPrize(item, settings.getId(), old.getStoreId());
            update.setId(old.getId()); update.setRemainNum(null); // 不覆盖并发扣减后的剩余库存。
            prizes.updateById(update);
            byPool.put(poolKey(old.getStoreId(), old.getCode()), old);
        }
        List<Long> pools = Objects.equals(settings.getPrizePoolRules(), 2) ? targets : Collections.singletonList(null);
        for (Long storeId : pools) for (LotteryPrizeReqVO item : template) {
            if (!byPool.containsKey(poolKey(storeId, item.getCode()))) {
                LotteryPrizeDO row = toPrize(item, settings.getId(), storeId);
                row.setRemainNum(0); prizes.insert(row);
                publisher.initializeNewStock(settings, row);
            }
        }
        try { settings.setPrizeTemplateJson(json.writeValueAsString(template)); }
        catch (Exception e) { throw new IllegalStateException(e); }
        settings.setConfigVersion(Optional.ofNullable(settings.getConfigVersion()).orElse(0L) + 1);
        if (settings.getStockEpoch() == null) settings.setStockEpoch(1L);
        if (settings.getRuntimeVersion() == null) settings.setRuntimeVersion(1);
        settingsMapper.updateById(settings);
        publisher.enqueue(settings);
    }

    /** 标签增删通知均按当前标签核对，允许重试与乱序。 */
    @Override
    public boolean refreshMembership(long activityId, long storeId) {
        long businessId = BusinessContextHolder.getRequiredBusinessId();
        LotterySettingsDO snapshot = settingsMapper.selectOne(new LambdaQueryWrapper<LotterySettingsDO>()
                .eq(LotterySettingsDO::getActivityId, activityId).eq(LotterySettingsDO::getBusinessId, businessId));
        if (snapshot == null) return false;
        if (!Objects.equals(snapshot.getRuntimeVersion(), 2)) return false; // 旧活动继续走原有门店标签同步。
        ActivityDO activity = activities.selectById(activityId);
        if (activity == null || !Objects.equals(activity.getAppScope(), 1)) return true;
        // 先远程查询标签，再开启数据库事务，避免持连接或行锁等待系统服务。
        boolean matched = matches(activityId, storeId);
        transactions.executeWithoutResult(status -> {
            LotterySettingsDO settings = settingsMapper.selectOne(new LambdaQueryWrapper<LotterySettingsDO>()
                    .eq(LotterySettingsDO::getId, snapshot.getId()).eq(LotterySettingsDO::getBusinessId, businessId).last("FOR UPDATE"));
            if (settings == null) return;
            if (!Objects.equals(settings.getConfigVersion(), snapshot.getConfigVersion()))
                throw new IllegalStateException("活动配置已变更，重新同步门店范围");
            ActivityDO current = activities.selectById(activityId);
            if (current == null || !Objects.equals(current.getAppScope(), 1)) return;
            var binding = new LambdaQueryWrapper<ActivityStoreDO>().eq(ActivityStoreDO::getActivityId, activityId)
                    .eq(ActivityStoreDO::getStoreId, storeId).eq(ActivityStoreDO::getBusinessId, businessId);
            boolean bound = activityStores.selectCount(binding) > 0;
            if (matched && !bound) {
                ActivityStoreDO row = new ActivityStoreDO(); row.setActivityId(activityId); row.setStoreId(storeId);
                row.setBusinessId(businessId); activityStores.insert(row);
                if (Objects.equals(settings.getPrizePoolRules(), 2)) ensureStorePrizes(settings, storeId);
            } else if (!matched && bound) activityStores.delete(binding);
            // activity_store_tag 保存活动配置，门店变更不能改写它。
            settings.setConfigVersion(Optional.ofNullable(settings.getConfigVersion()).orElse(0L) + 1);
            settingsMapper.updateById(settings);
            publisher.enqueue(settings);
        });
        return true;
    }

    private void ensureStorePrizes(LotterySettingsDO settings, long storeId) {
        Set<String> existingCodes = prizes.selectList(new LambdaQueryWrapper<LotteryPrizeDO>()
                .eq(LotteryPrizeDO::getLotteryId, settings.getId()).eq(LotteryPrizeDO::getStoreId, storeId))
                .stream().map(LotteryPrizeDO::getCode).collect(Collectors.toSet());
        List<LotteryPrizeReqVO> template = template(settings);
        if (template.isEmpty()) throw new IllegalStateException("缺少奖品模板，请先保存活动奖品配置");
        for (LotteryPrizeReqVO item : template) if (!existingCodes.contains(item.getCode())) {
            LotteryPrizeDO prize = toPrize(item, settings.getId(), storeId);
            prize.setRemainNum(0); prizes.insert(prize);
            publisher.initializeNewStock(settings, prize);
        }
    }

    @Override
    public boolean matches(long activityId, long storeId) {
        return Boolean.TRUE.equals(storeApi.matchStoreTagCache(BusinessContextHolder.getRequiredBusinessId(), storeId, tagIds(activityId)));
    }
    private static List<Long> normalize(List<Long> ids) {
        return ids == null ? List.of() : ids.stream().filter(Objects::nonNull).filter(id -> id > 0).distinct().sorted().toList();
    }
    private static String poolKey(Long storeId, String code) { return (storeId == null ? 0L : storeId) + ":" + code; }
    private static LotteryPrizeDO toPrize(LotteryPrizeReqVO item, Long settingsId, Long storeId) {
        LotteryPrizeDO row = BeanUtils.toBean(item, LotteryPrizeDO.class);
        row.setId(null); row.setLotteryId(settingsId); row.setStoreId(storeId);
        row.setWinningCitys(item.getWinningCitys() == null ? "" : String.join(",", item.getWinningCitys()));
        return row;
    }
}
