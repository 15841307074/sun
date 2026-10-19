package com.htyoudao.youdao.module.member.service.pointsLog;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.FrozenPointsReleaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@DS(DsNameConstants.SHARDING)
public class FrozenPointsReleaseServiceImpl implements FrozenPointsReleaseService {
    @Resource private FrozenPointsReleaseMapper mapper;
    @Override
    @DataPermission(enable = false)
    public List<PointsLogDO> pending(long businessId, int shard, long memberId, LocalDateTime before, LocalDateTime after,
                                    LocalDateTime cursorTime, Long cursorId, int limit) {
        return mapper.pending(businessId, shard, memberId, before, after, cursorTime, cursorId, limit);
    }

    @Override
    @DataPermission(enable = false)
    public Integer memberLevel(long memberId, long businessId, LocalDateTime before, LocalDateTime after) {
        long amount = mapper.pendingAmount(memberId, businessId, (int) (memberId % 10), before, after);
        return amount > 0 ? mapper.memberLevel(businessId, amount) : null;
    }

    @Override
    @DSTransactional
    @DataPermission(enable = false)
    public int release(long memberId, long businessId, List<Long> pointIds, LocalDateTime frozenBefore, LocalDateTime notBefore, Integer level) {
        if (pointIds.isEmpty()) return 0;
        if (pointIds.size() > 500) throw new IllegalArgumentException("解冻批次不能超过500条");
        int shard = (int) (memberId % 10);
        // 与下单积分写入保持一致：先锁会员，再锁明细；并发任务不能重复解冻同一笔积分。
        var member = mapper.lockMember(memberId, businessId, shard);
        if (member == null) throw new AccountUnavailable("解冻失败：会员不存在，memberId=" + memberId);
        var logs = mapper.lockDetails(memberId, businessId, shard, pointIds, frozenBefore, notBefore);
        if (logs.isEmpty()) return 0;
        long amount = 0;
        for (var row : logs) {
            if (row.getPointsChange() == null || row.getPointsChange() < 0)
                throw new IllegalStateException("冻结明细积分异常，pointsLogId=" + row.getPointsLogId());
            amount = Math.addExact(amount, row.getPointsChange());
        }
        long frozen = member.getFreezePoints() == null ? 0 : member.getFreezePoints();
        if (frozen < amount)
            throw new AccountUnavailable("冻结余额与明细不一致，memberId=" + memberId
                    + "，freezePoints=" + frozen + "，releasePoints=" + amount + "，pointsLogCount=" + logs.size());
        int available = Math.addExact(member.getMemberIntegral() == null ? 0 : member.getMemberIntegral(), Math.toIntExact(amount));
        long lifetime = Math.addExact(member.getIntegralFrozen() == null ? 0 : member.getIntegralFrozen(), amount);
        int changed;
        if (amount > 0) {
            changed = mapper.updateAccount(memberId, businessId, shard, available, frozen - amount, lifetime, level);
            if (changed != 1) throw new AccountUnavailable("解冻账户更新失败，memberId=" + memberId);
        }
        changed = mapper.updateDetails(memberId, businessId, shard, logs.stream().map(PointsLogDO::getPointsLogId).toList());
        if (changed != logs.size()) throw new IllegalStateException("解冻明细更新失败，memberId=" + memberId);
        return logs.size();
    }
}
