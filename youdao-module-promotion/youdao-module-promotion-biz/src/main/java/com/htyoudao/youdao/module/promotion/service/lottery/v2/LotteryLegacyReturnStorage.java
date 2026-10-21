package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
/** 旧奖品退回先持久化回执，缓存同步失败也可恢复，不会重复退库存。 */
@Service
@DS("sharding")
public class LotteryLegacyReturnStorage {
    @Resource private LotteryLogMapper logs;
    @Resource private JdbcTemplate jdbc;
    @Transactional(rollbackFor=Exception.class,timeout=5)
    public boolean claim(LotteryLogDO row,String primary,String legacy,String cache,String lock) {
        var where=new LambdaQueryWrapper<LotteryLogDO>().eq(LotteryLogDO::getId,row.getId()).eq(LotteryLogDO::getMemberId,row.getMemberId())
                .eq(LotteryLogDO::getBusinessId,row.getBusinessId()).eq(LotteryLogDO::getPrizeType,3).eq(LotteryLogDO::getPrizeState,1)
                .le(LotteryLogDO::getCreateTime,LocalDateTime.now(ZoneId.of("Asia/Shanghai")).minusHours(48))
                .and(a -> a.isNull(LotteryLogDO::getReceiveAddress).or().eq(LotteryLogDO::getReceiveAddress,""))
                .and(t -> t.isNull(LotteryLogDO::getTrackingNumber).or().eq(LotteryLogDO::getTrackingNumber,""));
        var update=new LotteryLogDO();update.setPrizeState(9);
        if(logs.update(update,where)!=1)return false;
        jdbc.update("UPDATE lottery_prize SET remain_num=GREATEST(COALESCE(remain_num,0)-1,0) WHERE id=? AND business_id=?",row.getLotteryPrizeId(),row.getBusinessId());
        jdbc.update("INSERT IGNORE INTO lottery_legacy_return(business_id,log_id,primary_stock_key,legacy_stock_key,prize_cache_key,lock_key) VALUES(?,?,?,?,?,?)",row.getBusinessId(),row.getId(),primary,legacy,cache,lock);
        return true;
    }
}
