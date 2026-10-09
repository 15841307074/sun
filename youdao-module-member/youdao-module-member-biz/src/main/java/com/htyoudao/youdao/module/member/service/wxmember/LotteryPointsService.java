package com.htyoudao.youdao.module.member.service.wxmember;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.PointsLogMapper;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/** 积分变更与幂等账本落在同一会员分片，并在同一事务提交。 */
@Service
@DS("sharding")
public class LotteryPointsService {
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private PointsLogMapper pointsLogs;
    @Transactional(rollbackFor=Exception.class)
    public void refund(long memberId,String drawBusinessNo) {
        if(memberId<=0||drawBusinessNo==null||!drawBusinessNo.matches("L[0-9]+"))throw new IllegalArgumentException("抽奖积分业务号不合法");
        var rows=jdbc.queryForList("SELECT delta,state FROM member_lottery_points WHERE business_id=? AND member_id=? AND sharding_value=? AND business_no=? FOR UPDATE",
                BusinessContextHolder.getRequiredBusinessId(),memberId,memberId%10,drawBusinessNo+":DEBIT");
        if(rows.isEmpty()||!"SUCCESS".equals(rows.get(0).get("state")))return;
        int delta=((Number)rows.get(0).get("delta")).intValue();
        if(delta>=0)throw new IllegalStateException("扣积分账本金额异常");
        if(!change(memberId,drawBusinessNo+":REFUND",Math.negateExact(delta)))throw new IllegalStateException("抽奖积分退款未完成");
    }
    @Transactional(rollbackFor=Exception.class)
    public boolean change(long memberId,String businessNo,int delta) {
        if(memberId<=0||businessNo==null||!businessNo.matches("L[0-9]+:(DEBIT|AWARD|REFUND)"))
            throw new IllegalArgumentException("抽奖积分业务号不合法");
        long b=BusinessContextHolder.getRequiredBusinessId(), shard=memberId%10;
        int inserted=jdbc.update("INSERT IGNORE INTO member_lottery_points(business_id,member_id,sharding_value,business_no,delta,state) VALUES(?,?,?,?,?,'INIT')",b,memberId,shard,businessNo,delta);
        Map<String,Object> row=jdbc.queryForMap("SELECT delta,state FROM member_lottery_points WHERE business_id=? AND member_id=? AND sharding_value=? AND business_no=? FOR UPDATE",b,memberId,shard,businessNo);
        if(((Number)row.get("delta")).intValue()!=delta)throw new IllegalArgumentException("相同积分业务号不能更改金额");
        // 已发放的奖励保持幂等；奖励被明确拒绝且没有入账时，允许修复后按原业务号补发。
        // 不重新受理被拒绝的消费扣减，人工补发也不会再次扣费。
        if(inserted==0 && !(businessNo.endsWith(":AWARD") && delta>0 && "REJECTED".equals(row.get("state"))))
            return "SUCCESS".equals(row.get("state"));
        int changed=jdbc.update("UPDATE wx_member SET member_integral=COALESCE(member_integral,0)+?,update_time=NOW() WHERE business_id=? AND member_id=? AND sharding_value=? AND deleted=0 AND COALESCE(member_integral,0)+? BETWEEN 0 AND 2147483647",delta,b,memberId,shard,delta);
        jdbc.update("UPDATE member_lottery_points SET state=? WHERE business_id=? AND member_id=? AND sharding_value=? AND business_no=?",changed==1?"SUCCESS":"REJECTED",b,memberId,shard,businessNo);
        if(changed==1) {
            Map<String,Object> member=jdbc.queryForMap("SELECT member_mobile,member_nick_name FROM wx_member WHERE business_id=? AND member_id=? AND sharding_value=?",b,memberId,shard);
            var log=new PointsLogDO();
            log.setPointsLogId(IdWorker.getId());log.setBusinessId(b);log.setMemberId(memberId);
            log.setMemberMobile((String)member.get("member_mobile"));log.setMemberNickName((String)member.get("member_nick_name"));
            log.setShardingValue((int)shard);log.setPointsChange((long)delta);log.setPointsType(delta<0?2:1);log.setPointsLogStatus(1);
            log.setLogCode(businessNo);log.setProductName(businessNo.endsWith(":REFUND")?"抽奖积分退回":delta<0?"抽奖消耗积分":"抽奖奖励积分");
            pointsLogs.insert(log); // 与积分变更同事务提交，重试不会重复写入流水。
        }
        return changed==1;
    }
}
