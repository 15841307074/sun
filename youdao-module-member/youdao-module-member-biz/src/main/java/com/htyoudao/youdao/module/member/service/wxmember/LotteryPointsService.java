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
import org.springframework.transaction.annotation.Isolation;
import java.util.Map;

/** 复用积分流水防重复；先锁会员，再处理流水和余额，在同一分片事务中提交。 */
@Service
@DS("sharding")
public class LotteryPointsService {
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private PointsLogMapper pointsLogs;
    @Transactional(rollbackFor=Exception.class, timeout=5, isolation=Isolation.READ_COMMITTED)
    public void refund(long memberId,String drawBusinessNo) {
        if(memberId<=0||drawBusinessNo==null||!drawBusinessNo.matches("L[0-9]+"))throw new IllegalArgumentException("抽奖积分业务号不合法");
        long businessId=BusinessContextHolder.getRequiredBusinessId();
        Map<String,Object> member=lockMember(businessId,memberId);
        Map<String,Object> debit=findLog(businessId,memberId,drawBusinessNo+":DEBIT");
        if(debit==null||!successful(debit))return;
        int delta=Math.toIntExact(((Number)debit.get("points_change")).longValue());
        if(delta>=0)throw new IllegalStateException("扣积分流水金额异常");
        if(member==null||!changeLocked(businessId,memberId,drawBusinessNo+":REFUND",Math.negateExact(delta),member))
            throw new IllegalStateException("抽奖积分退款未完成");
    }
    @Transactional(rollbackFor=Exception.class, timeout=5, isolation=Isolation.READ_COMMITTED)
    public boolean change(long memberId,String businessNo,int delta) {
        if(memberId<=0||businessNo==null||!businessNo.matches("L[0-9]+:(DEBIT|AWARD|REFUND)"))
            throw new IllegalArgumentException("抽奖积分业务号不合法");
        long businessId=BusinessContextHolder.getRequiredBusinessId();
        Map<String,Object> member=lockMember(businessId,memberId);
        return member!=null&&changeLocked(businessId,memberId,businessNo,delta,member);
    }

    private Map<String,Object> lockMember(long businessId,long memberId) {
        var rows=jdbc.queryForList("SELECT member_mobile,member_nick_name FROM wx_member WHERE business_id=? AND member_id=? AND sharding_value=? AND deleted=0 FOR UPDATE",
                businessId,memberId,memberId%10);
        if(rows.size()>1)throw new IllegalStateException("会员积分账户重复");
        return rows.isEmpty()?null:rows.get(0);
    }

    private Map<String,Object> findLog(long businessId,long memberId,String businessNo) {
        // 账户行已锁定，READ_COMMITTED 保证重试读到最新流水；不锁空流水范围，避免不同会员间的间隙锁死锁。
        // 包括软删除流水：隐藏历史明细不能使同一操作重新扣款或入账。
        var rows=jdbc.queryForList("SELECT points_log_id,points_change,points_log_status FROM points_log WHERE business_id=? AND member_id=? AND sharding_value=? AND log_code=?",
                businessId,memberId,memberId%10,businessNo);
        if(rows.size()>1)throw new IllegalStateException("抽奖积分业务流水重复");
        return rows.isEmpty()?null:rows.get(0);
    }

    private boolean successful(Map<String,Object> log) {
        return log.get("points_log_status") instanceof Number status&&(status.intValue()==1||status.intValue()==2);
    }

    private boolean changeLocked(long businessId,long memberId,String businessNo,int delta,Map<String,Object> member) {
        Map<String,Object> existing=findLog(businessId,memberId,businessNo);
        if(existing!=null) {
            if(((Number)existing.get("points_change")).longValue()!=delta)throw new IllegalArgumentException("相同积分业务号不能更改金额");
            if(successful(existing))return true;
            // 失败扣款不重新扣费；未到账的奖励、退款允许修复后重试。
            if(businessNo.endsWith(":DEBIT"))return false;
        }
        int changed=jdbc.update("UPDATE wx_member SET member_integral=COALESCE(member_integral,0)+?,update_time=NOW() WHERE business_id=? AND member_id=? AND sharding_value=? AND deleted=0 AND COALESCE(member_integral,0)+? BETWEEN 0 AND 2147483647",
                delta,businessId,memberId,memberId%10,delta);
        if(existing==null) {
            var log=new PointsLogDO();
            log.setPointsLogId(IdWorker.getId());log.setBusinessId(businessId);log.setMemberId(memberId);
            log.setMemberMobile((String)member.get("member_mobile"));log.setMemberNickName((String)member.get("member_nick_name"));
            // 抽奖扣积分与商品兑换分开标记；PC 兑换记录仅查询类型 2、3。
            log.setShardingValue((int)(memberId%10));log.setPointsChange((long)delta);log.setPointsType(businessNo.endsWith(":DEBIT")?5:delta<0?2:1);
            log.setIsPointsProduct(2);log.setPointsLogStatus(changed==1?1:0);log.setDeleted(changed!=1);
            log.setLogCode(businessNo);log.setProductName(businessNo.endsWith(":REFUND")?"抽奖积分退回":delta<0?"抽奖消耗积分":"抽奖奖励积分");
            // 状态0为内部失败标记，软删除使它不出现在用户明细；成功流水沿用状态1。
            if(pointsLogs.insert(log)!=1)throw new IllegalStateException("抽奖积分流水未保存");
        } else if(changed==1) {
            if(jdbc.update("UPDATE points_log SET points_log_status=1,deleted=0,update_time=NOW() WHERE business_id=? AND member_id=? AND sharding_value=? AND points_log_id=? AND points_log_status=0",
                    businessId,memberId,memberId%10,existing.get("points_log_id"))!=1)
                throw new IllegalStateException("抽奖积分流水未更新");
        }
        return changed==1;
    }
}
