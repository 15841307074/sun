package com.htyoudao.youdao.module.member.service.pointsLog;

import java.time.LocalDateTime;
import java.util.List;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;

/** 按会员原子解冻账户和对应积分明细。 */
public interface FrozenPointsReleaseService {
    List<PointsLogDO> pending(long businessId, int shard, long memberId, LocalDateTime before, LocalDateTime after,
                            LocalDateTime cursorTime, Long cursorId, int limit);
    Integer memberLevel(long memberId, long businessId, LocalDateTime before, LocalDateTime after);
    int release(long memberId, long businessId, List<Long> pointIds, LocalDateTime frozenBefore, LocalDateTime notBefore, Integer memberLevel);

    /** 账户缺失或账务不一致，不能把本次候选记录盲目当作可发积分。 */
    class AccountUnavailable extends IllegalStateException {
        public AccountUnavailable(String message) { super(message); }
    }
}
