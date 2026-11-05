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
        return releaseChecked(memberId, businessId, pointIds, frozenBefore, notBefore, level, false).released();
    }

    @Override
    @DSTransactional
    @DataPermission(enable = false)
    public ReleaseResult releaseChecked(long memberId, long businessId, List<Long> pointIds,
                                         LocalDateTime frozenBefore, LocalDateTime notBefore, Integer level, boolean resolveLevel) {
        if (pointIds.isEmpty()) return new ReleaseResult(0, null, 0, 0, level, null);
        if (pointIds.size() > 500) throw new IllegalArgumentException("解冻批次不能超过500条");
        int shard = (int) (memberId % 10);
        // 与下单积分写入保持一致：先锁会员，再锁明细；并发任务不能重复解冻同一笔积分。
        var member = mapper.lockMember(memberId, businessId, shard);
        if (member == null) throw new AccountUnavailable("解冻失败：会员不存在，memberId=" + memberId);
        var logs = mapper.lockDetails(memberId, businessId, shard, pointIds, frozenBefore, notBefore);
        if (logs.isEmpty()) return new ReleaseResult(0, member.getFreezePoints(), 0, 0, level, null);
        long amount = 0;
        for (var row : logs) {
            if (row.getPointsChange() == null || row.getPointsChange() < 0)
                throw new IllegalStateException("冻结明细积分异常，pointsLogId=" + row.getPointsLogId());
            try { amount = Math.addExact(amount, row.getPointsChange()); }
            catch (ArithmeticException overflow) { throw new AccountUnavailable("冻结明细合计溢出，memberId=" + memberId); }
        }
        var balance = mapper.frozenBalance(memberId, businessId, shard, frozenBefore, notBefore);
        if (balance == null || balance.invalidCount() > 0 || balance.total() < amount || balance.total() < 0)
            throw new AccountUnavailable("冻结明细存在空值、负数或合计异常，拒绝自动修复，memberId=" + memberId);
        long frozen = balance.total();
        String levelWarning = null;
        if (resolveLevel && balance.matureTotal() > 0) {
            try { level = mapper.memberLevel(businessId, balance.matureTotal()); }
            catch (org.springframework.dao.DataAccessResourceFailureException unavailable) { throw unavailable; }
            catch (org.springframework.dao.TransientDataAccessException timeout) { throw timeout; }
            catch (RuntimeException error) { levelWarning = error.getMessage(); }
        }
        int available;
        long lifetime;
        try {
            available = Math.addExact(member.getMemberIntegral() == null ? 0 : member.getMemberIntegral(), Math.toIntExact(amount));
            lifetime = Math.addExact(member.getIntegralFrozen() == null ? 0 : member.getIntegralFrozen(), amount);
        } catch (ArithmeticException overflow) {
            throw new AccountUnavailable("解冻后账户积分超出存储范围，memberId=" + memberId);
        }
        if (available < 0 || lifetime < 0) throw new AccountUnavailable("账户可用或累计积分异常，memberId=" + memberId);
        if (available < 0 || lifetime < 0) throw new AccountUnavailable("账户可用或累计积分异常，memberId=" + memberId);
        int changed;
        if (amount > 0 || member.getFreezePoints() == null || member.getFreezePoints() != frozen) {
            changed = mapper.updateAccount(memberId, businessId, shard, available, frozen - amount, lifetime, level);
            if (changed != 1) throw new AccountUnavailable("解冻账户更新失败，memberId=" + memberId);
        }
        changed = mapper.updateDetails(memberId, businessId, shard, logs.stream().map(PointsLogDO::getPointsLogId).toList());
        if (changed != logs.size()) throw new IllegalStateException("解冻明细更新失败，memberId=" + memberId);
        return new ReleaseResult(logs.size(), member.getFreezePoints(), frozen, frozen - amount, level, levelWarning);
    }
}
