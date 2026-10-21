package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.*;
/** 仅按需执行的历史映射补齐；分页和游标避免全表长事务。 */
@Service
@DS("sharding")
public class LotteryIdentityBackfillService {
    @Resource private LotteryLogMapper logs;
    @Resource private LotterySettingsMapper settings;
    @Resource private LotteryIdentityService identities;
    public record Progress(long lastId,int scanned,int mapped,boolean more){}
    @DataPermission(enable=false)
    public Progress run(long afterId,int maxRows) {
        long business=BusinessContextHolder.getRequiredBusinessId();
        if(afterId<0||maxRows<1||maxRows>10000)throw new IllegalArgumentException("补齐每次最多10000条，游标不能为负数");
        Map<Long,Long> activities=new HashMap<>();
        for(var cfg:settings.selectList(new LambdaQueryWrapper<LotterySettingsDO>().eq(LotterySettingsDO::getBusinessId,business))) {
            activities.put(cfg.getId(),cfg.getActivityId());activities.put(cfg.getActivityId(),cfg.getActivityId());
        }
        int scanned=0,mapped=0;long cursor=afterId;boolean more=true;
        while(scanned<maxRows&&!Thread.currentThread().isInterrupted()) {
            int size=Math.min(200,maxRows-scanned);
            var batch=logs.selectLotteryRows(new LambdaQueryWrapper<LotteryLogDO>().eq(LotteryLogDO::getBusinessId,business).eq(LotteryLogDO::getDeleted,false)
                    .gt(LotteryLogDO::getId,cursor).orderByAsc(LotteryLogDO::getId).last("LIMIT "+size));
            if(batch.isEmpty()){more=false;break;}
            for(var row:batch) {
                cursor=Math.max(cursor,row.getId());scanned++;
                if(row.getMemberMobile()==null||row.getMemberMobile().isBlank()||row.getMemberId()==null||row.getLotteryId()==null)continue;
                identities.register(activities.getOrDefault(row.getLotteryId(),row.getLotteryId()),row.getMemberId(),row.getMemberMobile(),null);mapped++;
            }
            if(batch.size()<size){more=false;break;}
        }
        return new Progress(cursor,scanned,mapped,more);
    }
}
