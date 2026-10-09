package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

/** 仅使用主库；事务内不等待 Redis 锁、远程调用或线程池。 */
@Service("lotteryLedger")
@DS("master")
public class LotteryLedgerImpl implements LotteryLedger {
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private ObjectMapper json;
    @Resource
    private LotteryCounterCache quotaCache;
    @Override
    public String encode(Object value) {
        try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}
    }
    private <T>T decode(String value,Class<T> type) {
        try{return json.readValue(value,type);}catch(Exception e){throw new IllegalStateException(e);}
    }
    private Draw read(Map<String,Object> r) {
        Draw d=new Draw(); d.setId(n(r,"id"));d.setBusinessId(n(r,"business_id"));d.setActivityId(n(r,"activity_id"));
        d.setMemberId(n(r,"member_id"));d.setStoreId(n(r,"store_id"));d.setEpoch(n(r,"stock_epoch"));d.setPool(n(r,"pool_store_id"));
        d.setRequestId((String)r.get("request_id"));d.setOutBillNo((String)r.get("out_bill_no"));
        d.setStockPrizeCode((String)r.get("prize_code"));
        d.setDrawStatus((String)r.get("draw_status"));d.setGrantStatus((String)r.get("grant_status"));d.setStockState((String)r.get("stock_state"));
        d.setScopeKey((String)r.get("scope_key"));d.setSource((int)n(r,"chance_source"));d.setPointsCost((int)n(r,"points_cost"));
        d.setSnapshot(decode((String)r.get("snapshot_json"),LotteryDrawSnapshot.class));
        if(r.get("result_json")!=null)d.setResult(decode((String)r.get("result_json"),LotteryUserLogVO.class));
        return d;
    }
    private static long n(Map<String,Object> r,String key){return ((Number)r.get(key)).longValue();}
    /** JDBC may expose BIT/TINYINT(1) as Boolean; state is a flag, not an ID or counter. */
    private static boolean enabledState(Object state) {
        return Boolean.TRUE.equals(state) || (state instanceof Number value && value.longValue() == 1);
    }
    @Override
    public Draw find(long b,long a,long m,String requestId) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM lottery_v2_request WHERE business_id=? AND activity_id=? AND member_id=? AND request_id=?",b,a,m,requestId);
        return rows.isEmpty()?null:read(rows.get(0));
    }
    @Override
    public Draw findByReference(long b,long ref,long m,String requestId) {
        var rows=jdbc.queryForList("SELECT * FROM lottery_v2_request WHERE business_id=? AND member_id=? AND request_id=? AND (activity_id=? OR settings_id=?)",b,m,requestId,ref,ref);
        return rows.isEmpty()?null:read(rows.get(0));
    }
    @Override
    public void fillDisplayStock(long businessId,long activityId,long epoch,List<LotteryPrizeDO> prizes) {
        if(prizes.isEmpty())return;
        List<Long> pools=prizes.stream().map(p->p.getStoreId()==null?0L:p.getStoreId()).distinct().toList();
        List<Object> args=new ArrayList<>(List.of(businessId,activityId,epoch));args.addAll(pools);
        Map<Long,Long> used=new HashMap<>();
        jdbc.queryForList("SELECT prize_id,reserved+issued AS used FROM lottery_v2_stock WHERE business_id=? AND activity_id=? AND stock_epoch=? AND pool_store_id IN ("+String.join(",",Collections.nCopies(pools.size(),"?"))+")",args.toArray())
                .forEach(row->used.put(n(row,"prize_id"),n(row,"used")));
        prizes.forEach(prize->prize.setRemainNum(Math.toIntExact(used.getOrDefault(prize.getId(),0L))));
    }
    @Override
    public Draw get(long id) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM lottery_v2_request WHERE id=?",id);
        return rows.isEmpty()?null:read(rows.get(0));
    }
    @Override
    public Draw byBill(String bill) {
        if (bill != null && bill.matches("L[0-9]{15,20}F?R[1-9][0-9]*")) {
            Draw original = byBill(bill.substring(0,bill.lastIndexOf('R')));
            return original != null && bill.equals(original.cashBillNo()) ? original : null;
        }
        if (bill != null && bill.endsWith("F")) {
            Draw original = byBill(bill.substring(0, bill.length()-1));
            return original != null && original.getSnapshot().isFallbackApplied() ? original : null;
        }
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM lottery_v2_request WHERE out_bill_no=?",bill);
        return rows.isEmpty()?null:read(rows.get(0));
    }
    @Override
    public Stock stock(long b,long a,long epoch,long pool,String code) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT total,reserved,issued,revision FROM lottery_v2_stock WHERE business_id=? AND activity_id=? AND stock_epoch=? AND pool_store_id=? AND prize_code=?",b,a,epoch,pool,code);
        if(rows.isEmpty())return null;
        Map<String,Object> r=rows.get(0);return new Stock(n(r,"total"),n(r,"reserved"),n(r,"issued"),n(r,"revision"));
    }
    private void ensureCounter(long b,long a,long m,String scope,int source) {
        jdbc.update("INSERT IGNORE INTO lottery_v2_counter(business_id,activity_id,member_id,scope_key,source) VALUES(?,?,?,?,?)",b,a,m,scope,source);
    }
    @Override
    public long consumed(long b,long a,long m,String scope,int source) {
        List<Long> values=jdbc.queryForList("SELECT consumed FROM lottery_v2_counter WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key=? AND source=?",Long.class,b,a,m,scope,source);
        return values.isEmpty()?0:values.get(0);
    }
    @Override
    public long activityDrawCount(long b,long a) {
        var totals=jdbc.queryForList("SELECT total FROM lottery_v2_activity_counter WHERE business_id=? AND activity_id=?",Long.class,b,a);
        return totals.isEmpty()?0:totals.get(0);
    }
    @Override
    public long[] counter(long b,long a,long m,String scope,int source) {
        List<Map<String,Object>> values=jdbc.queryForList("SELECT gained,consumed,finished FROM lottery_v2_counter WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key=? AND source=?",b,a,m,scope,source);
        if(values.isEmpty())return new long[]{0,0,0};
        Map<String,Object> r=values.get(0);return new long[]{n(r,"gained"),n(r,"consumed"),n(r,"finished")};
    }
    @Override
    public Map<String,long[]> counters(long b,long a,long m,String period) {
        Map<String,long[]> result=new HashMap<>();
        for(var row:jdbc.queryForList("SELECT scope_key,source,gained,consumed,finished FROM lottery_v2_counter WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key IN ('TOTAL',?)",b,a,m,period))
            result.put(row.get("scope_key")+":"+n(row,"source"),new long[]{n(row,"gained"),n(row,"consumed"),n(row,"finished")});
        return result;
    }
    private void consumeBound(long b,long a,long m,String scope,int source,long limit) {
        ensureCounter(b,a,m,scope,source);
        if(jdbc.update("UPDATE lottery_v2_counter SET consumed=consumed+1,revision=revision+1 WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key=? AND source=? AND (?<=0 OR consumed<?)",b,a,m,scope,source,limit,limit)!=1)
            throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public Draw accept(long b,LotteryDrawSnapshot snapshot) {
        var req=snapshot.getRequest();var cfg=snapshot.getSettings();
        long a=cfg.getActivityId(),m=req.getMemberId(),pool=Objects.equals(cfg.getPrizePoolRules(),2)?req.getStoreId():0;
        // 共享读锁允许并发抽奖，同时阻止配置版本切换。
        List<Map<String,Object>> configs=jdbc.queryForList("SELECT config_version,stock_epoch,runtime_version,state FROM lottery_settings WHERE id=? AND business_id=? AND deleted=0 LOCK IN SHARE MODE",cfg.getId(),b);
        if(configs.isEmpty()||n(configs.get(0),"config_version")!=cfg.getConfigVersion()||n(configs.get(0),"stock_epoch")!=cfg.getStockEpoch()
                ||n(configs.get(0),"runtime_version")!=2||!enabledState(configs.get(0).get("state")))throw exception(LOTTERY_SYSTEM_AGAIN);
        Draw previous=find(b,a,m,req.getRequestId());if(previous!=null)return previous;
        consumeBound(b,a,m,"TOTAL",0,Optional.ofNullable(cfg.getLotteryTotalNumber()).orElse(0));
        consumeBound(b,a,m,snapshot.getScopeKey(),0,Optional.ofNullable(cfg.getLotteryLimit()).orElse(0));
        LotteryDrawSnapshot.Chance chosen=null;
        for(var chance:snapshot.getChances()) {
            ensureCounter(b,a,m,chance.getScope(),chance.getSource());
            int count=jdbc.update("UPDATE lottery_v2_counter SET consumed=consumed+1 WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key=? AND source=? " +
                    (chance.isEarned()?"AND consumed<gained":"AND (?<0 OR consumed<?)"),
                    chance.isEarned()?new Object[]{b,a,m,chance.getScope(),chance.getSource()}:new Object[]{b,a,m,chance.getScope(),chance.getSource(),chance.getLimit(),chance.getLimit()});
            if(count==1){chosen=chance;break;}
        }
        if(chosen==null)throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
        LotteryPrizeDO prize=snapshot.getPrize(); boolean stocked=!Objects.equals(prize.getIsGuarantees(),1);
        if(stocked&&Objects.equals(prize.getIsRepeat(),0)) {
            // 使用当前读，避免事务先前的快照读漏掉并发中奖记录。
            var won=jdbc.queryForList("SELECT id FROM lottery_v2_request WHERE business_id=? AND activity_id=? AND member_id=? AND prize_code=? AND stock_state IN ('RESERVED','ISSUED','RETURNED') LIMIT 1 FOR UPDATE",Long.class,b,a,m,prize.getCode());
            if(!won.isEmpty())throw new UnavailablePrize();
        }
        if(stocked&&jdbc.update("UPDATE lottery_v2_stock SET reserved=reserved+1,revision=revision+1 WHERE business_id=? AND activity_id=? AND stock_epoch=? AND pool_store_id=? AND prize_code=? AND reserved+issued<total",
                b,a,cfg.getStockEpoch(),pool,prize.getCode())!=1)throw new UnavailablePrize();
        long id=IdWorker.getId();
        jdbc.update("INSERT INTO lottery_v2_request(id,business_id,activity_id,settings_id,member_id,store_id,request_id,config_version,stock_epoch,pool_store_id,prize_code,prize_id,stock_state,chance_source,scope_key,points_cost,out_bill_no,snapshot_json) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id,b,a,cfg.getId(),m,req.getStoreId(),req.getRequestId(),cfg.getConfigVersion(),cfg.getStockEpoch(),pool,prize.getCode(),prize.getId(),stocked?"RESERVED":"NONE",chosen.getSource(),chosen.getScope(),chosen.getPointsCost(),"L"+id,encode(snapshot));
        jdbc.update("INSERT INTO lottery_v2_job(request_pk) VALUES(?)",id);
        jdbc.update("INSERT INTO lottery_v2_activity_counter(business_id,activity_id,total) VALUES(?,?,1) ON DUPLICATE KEY UPDATE total=total+1",b,a);
        quotaCache.changed(b,a,m);
        // 插入所需字段均已确定，无需立即重读并反序列化。
        Draw accepted=new Draw(); accepted.setId(id); accepted.setBusinessId(b); accepted.setActivityId(a);
        accepted.setMemberId(m); accepted.setStoreId(req.getStoreId()); accepted.setEpoch(cfg.getStockEpoch()); accepted.setPool(pool);
        accepted.setRequestId(req.getRequestId()); accepted.setOutBillNo("L"+id);
        accepted.setDrawStatus("ACCEPTED"); accepted.setGrantStatus("PENDING"); accepted.setStockState(stocked?"RESERVED":"NONE");
        accepted.setScopeKey(chosen.getScope()); accepted.setSource(chosen.getSource()); accepted.setPointsCost(chosen.getPointsCost());
        accepted.setStockPrizeCode(prize.getCode()); accepted.setSnapshot(snapshot); accepted.setNewlyAccepted(true);
        return accepted;
    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public void grantState(long id,String grant,LotteryUserLogVO result,boolean terminal,boolean success) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM lottery_v2_request WHERE id=? FOR UPDATE",id);
        if(rows.isEmpty())throw new IllegalArgumentException("抽奖流水不存在");
        Draw draw=read(rows.get(0));
        if(!"ACCEPTED".equals(draw.getDrawStatus()))return;
        if(terminal&&"RESERVED".equals(draw.getStockState())) {
            int changed=jdbc.update("UPDATE lottery_v2_stock SET reserved=reserved-1,issued=issued+?,revision=revision+1 WHERE business_id=? AND activity_id=? AND stock_epoch=? AND pool_store_id=? AND prize_code=? AND reserved>0",
                    success?1:0,draw.getBusinessId(),draw.getActivityId(),draw.getEpoch(),draw.getPool(),draw.getSnapshot().getPrize().getCode());
            if(changed!=1)throw new IllegalStateException("库存账本与流水不一致");
        }
        if(result!=null){result.setRequestId(draw.getRequestId());result.setGrantStatus(grant);}
        jdbc.update("UPDATE lottery_v2_request SET grant_status=?,draw_status=?,stock_state=?,result_json=? WHERE id=?",
                grant,terminal?(success?"COMPLETED":"FAILED"):"ACCEPTED",terminal&&"RESERVED".equals(draw.getStockState())?(success?"ISSUED":"RELEASED"):draw.getStockState(),result==null?null:encode(result),id);
    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public void rejectUnpaid(long id) {
        Draw draw=get(id);
        if(draw==null||!"ACCEPTED".equals(draw.getDrawStatus()))return;
        if(draw.getSnapshot().isManualReissue())return;
        // 退款与受理使用相同的会员、周期、来源、库存加锁顺序。
        for(Object[] counter:new Object[][]{{"TOTAL",0},{draw.getSnapshot().getScopeKey(),0},{draw.getScopeKey(),draw.getSource()}})
            jdbc.queryForObject("SELECT consumed FROM lottery_v2_counter WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key=? AND source=? FOR UPDATE",Long.class,draw.getBusinessId(),draw.getActivityId(),draw.getMemberId(),counter[0],counter[1]);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM lottery_v2_request WHERE id=? FOR UPDATE",id);
        if(!"ACCEPTED".equals(rows.get(0).get("draw_status")))return;
        grantState(id,"FAILED",null,true,false);
        for(Object[] counter:new Object[][]{{"TOTAL",0},{draw.getSnapshot().getScopeKey(),0},{draw.getScopeKey(),draw.getSource()}})
            jdbc.update("UPDATE lottery_v2_counter SET consumed=consumed-1,revision=revision+1 WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key=? AND source=? AND consumed>0",draw.getBusinessId(),draw.getActivityId(),draw.getMemberId(),counter[0],counter[1]);
        jdbc.update("UPDATE lottery_v2_activity_counter SET total=total-1 WHERE business_id=? AND activity_id=? AND total>0",draw.getBusinessId(),draw.getActivityId());
        quotaCache.changed(draw.getBusinessId(),draw.getActivityId(),draw.getMemberId());
    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public void fallback(long id) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM lottery_v2_request WHERE id=? FOR UPDATE",id);
        if(rows.isEmpty()||!"ACCEPTED".equals(rows.get(0).get("draw_status")))return;
        Draw draw=read(rows.get(0));
        if(draw.getSnapshot().isFallbackApplied())return;
        if(draw.getSnapshot().isManualReissue())throw new IllegalStateException("人工补发不得更换原奖品");
        if("RESERVED".equals(draw.getStockState())) {
            if(jdbc.update("UPDATE lottery_v2_stock SET reserved=reserved-1,revision=revision+1 WHERE business_id=? AND activity_id=? AND stock_epoch=? AND pool_store_id=? AND prize_code=? AND reserved>0",draw.getBusinessId(),draw.getActivityId(),draw.getEpoch(),draw.getPool(),draw.getSnapshot().getPrize().getCode())!=1)
                throw new IllegalStateException("红包失败库存释放不一致");
        }
        draw.getSnapshot().setPrize(draw.getSnapshot().getGuarantee());
        draw.getSnapshot().setCoupon(draw.getSnapshot().getGuaranteeCoupon());
        draw.getSnapshot().setFallbackApplied(true);
        jdbc.update("UPDATE lottery_v2_request SET stock_state='RELEASED',grant_status='PENDING',snapshot_json=?,result_json=NULL WHERE id=?",encode(draw.getSnapshot()),id);
    }

    @Override
    public boolean wakeByBill(String bill) {
        Draw draw=byBill(bill);
        if(draw==null) {
            // 已验签的历史补发回调只确认收到，不能唤醒或改写当前补发单。
            if(bill!=null&&bill.matches("L[0-9]{15,20}F?R[1-9][0-9]{0,4}")) {
                int split=bill.lastIndexOf('R');
                Draw original=byBill(bill.substring(0,split));
                if(original!=null&&Long.parseLong(bill.substring(split+1))<original.getSnapshot().getReissueSequence())return true;
            }
            return false;
        }
        jdbc.update("UPDATE lottery_v2_job SET next_at=NOW(3) WHERE request_pk=? AND state='READY'",draw.getId());
        return true;
    }

    /** 锁定原请求和任务，只补发原快照；不触碰次数计数和扣积分业务号。 */
    @Transactional(rollbackFor=Exception.class)
    @Override
    public Reissue reissue(long b,long id,long member,long store,String requestId,String expectedBill,
                           long expectedSequence,boolean cashDefinitelyFailed) {
        var rows=jdbc.queryForList("SELECT * FROM lottery_v2_request WHERE id=? AND business_id=? AND member_id=? AND request_id=? FOR UPDATE",id,b,member,requestId);
        if(rows.isEmpty())throw exception(LOTTERY_REISSUE_REJECTED,"原抽奖流水不存在或不属于当前项目/会员");
        Draw draw=read(rows.get(0));
        if(draw.getStoreId()!=store)throw exception(LOTTERY_REISSUE_REJECTED,"门店与原抽奖不一致");
        if("RETURNED".equals(draw.getDrawStatus())||"RETURNED".equals(draw.getStockState()))throw exception(LOTTERY_REISSUE_REJECTED,"已退奖记录不能补发");
        if("COMPLETED".equals(draw.getDrawStatus())||"SUCCESS".equals(draw.getGrantStatus()))return new Reissue(draw,false,"奖品已发放，不重复补发");
        var jobs=jdbc.queryForList("SELECT state,action FROM lottery_v2_job WHERE request_pk=? FOR UPDATE",id);
        if(jobs.isEmpty())throw new IllegalStateException("原发奖任务不存在，需核对流水");
        if("RETURN".equals(jobs.get(0).get("action")))throw exception(LOTTERY_REISSUE_REJECTED,"退奖任务不能补发");
        if("RUNNING".equals(jobs.get(0).get("state")))return new Reissue(draw,false,"发奖任务正在执行，请稍后查询");
        if("READY".equals(jobs.get(0).get("state"))&&draw.getSnapshot().isManualReissue()&&"ACCEPTED".equals(draw.getDrawStatus()))
            return new Reissue(draw,false,"补发任务已安排，不重复安排");
        if(!Objects.equals(expectedBill,draw.cashBillNo())||expectedSequence!=draw.getSnapshot().getReissueSequence())
            return new Reissue(draw,false,"原请求已被其他操作更新，请重新查询");
        if(!"ACCEPTED".equals(draw.getDrawStatus())&&!"FAILED".equals(draw.getDrawStatus()))throw exception(LOTTERY_REISSUE_REJECTED,"当前流水状态不允许补发");
        if("ISSUED".equals(draw.getStockState()))throw new IllegalStateException("库存已记为发放，需人工核对，不能重复补发");
        boolean stocked=!Objects.equals(draw.getSnapshot().getPrize().getIsGuarantees(),1);
        if(stocked&&"RELEASED".equals(draw.getStockState())) {
            int changed=jdbc.update("UPDATE lottery_v2_stock SET reserved=reserved+1,revision=revision+1 WHERE business_id=? AND activity_id=? AND stock_epoch=? AND pool_store_id=? AND prize_code=? AND reserved+issued<total",b,draw.getActivityId(),draw.getEpoch(),draw.getPool(),draw.getStockPrizeCode());
            if(changed!=1)throw exception(LOTTERY_REISSUE_REJECTED,"原奖品批次库存不足或已不存在，不能补发");
            draw.setStockState("RESERVED");
        } else if(stocked&&!"RESERVED".equals(draw.getStockState()))throw new IllegalStateException("原奖品库存状态异常，不能补发");
        long sequence=Math.addExact(draw.getSnapshot().getReissueSequence(),1);
        if(sequence>99999)throw exception(LOTTERY_REISSUE_REJECTED,"人工补发次数异常，需核对流水");
        draw.getSnapshot().setManualReissue(true);
        draw.getSnapshot().setReissueSequence(sequence);
        if(Objects.equals(draw.getSnapshot().getPrize().getPrizeType(),5)&&cashDefinitelyFailed) {
            String base=draw.getSnapshot().isFallbackApplied()?draw.getOutBillNo()+"F":draw.getOutBillNo();
            draw.getSnapshot().setReissueCashBillNo(base+"R"+sequence);
        }
        draw.setDrawStatus("ACCEPTED"); draw.setGrantStatus("PENDING"); draw.setResult(null);
        jdbc.update("UPDATE lottery_v2_request SET draw_status='ACCEPTED',grant_status='PENDING',stock_state=?,snapshot_json=?,result_json=NULL WHERE id=?",draw.getStockState(),encode(draw.getSnapshot()),id);
        jdbc.update("UPDATE lottery_v2_job SET state='READY',action='GRANT',lease_token=NULL,lease_until=NULL,next_at=NOW(3),last_error=NULL WHERE request_pk=?",id);
        return new Reissue(draw,true,"已安排人工补发，不扣抽奖次数或消耗积分");
    }
    @Override
    public boolean queuePhysicalReturn(LotteryLogDO log) {
        Draw draw=get(log.getId());
        if(draw==null)return false;
        if(draw.getBusinessId()!=log.getBusinessId()||draw.getMemberId()!=log.getMemberId())throw new IllegalArgumentException("退奖记录与流水不一致");
        jdbc.update("UPDATE lottery_v2_job SET action='RETURN',state='READY',next_at=NOW(3) WHERE request_pk=? AND state='DONE'",draw.getId());
        return true;
    }
    @Override
    public boolean isReturnJob(long id){return "RETURN".equals(jdbc.queryForObject("SELECT action FROM lottery_v2_job WHERE request_pk=?",String.class,id));}
    @Transactional(rollbackFor=Exception.class)
    @Override
    public void confirmPhysicalReturn(long id) {
        var rows=jdbc.queryForList("SELECT * FROM lottery_v2_request WHERE id=? FOR UPDATE",id);
        if(rows.isEmpty())return;
        Draw draw=read(rows.get(0));
        if(!"COMPLETED".equals(draw.getDrawStatus())||!Objects.equals(draw.getSnapshot().getPrize().getPrizeType(),3))return;
        if("ISSUED".equals(draw.getStockState())) {
            int changed=jdbc.update("UPDATE lottery_v2_stock SET issued=issued-1,revision=revision+1 WHERE business_id=? AND activity_id=? AND stock_epoch=? AND pool_store_id=? AND prize_code=? AND issued>0",
                    draw.getBusinessId(),draw.getActivityId(),draw.getEpoch(),draw.getPool(),draw.getStockPrizeCode());
            if(changed!=1)throw new IllegalStateException("退奖库存与流水不一致");
        }
        jdbc.update("UPDATE lottery_v2_request SET draw_status='RETURNED',grant_status='RETURNED',stock_state=CASE WHEN stock_state='ISSUED' THEN 'RETURNED' ELSE stock_state END,result_json=NULL WHERE id=?",id);
    }

    @Override
    public List<Long> due(int count) {return jdbc.queryForList("SELECT request_pk FROM lottery_v2_job WHERE (state='READY' AND next_at<=NOW(3)) OR (state='RUNNING' AND lease_until<NOW(3)) ORDER BY next_at LIMIT ?",Long.class,count);}
    @Override
    public boolean claim(long id,String token) {return jdbc.update("UPDATE lottery_v2_job SET state='RUNNING',lease_token=?,lease_until=DATE_ADD(NOW(3),INTERVAL 60 SECOND),attempts=attempts+1 WHERE request_pk=? AND ((state='READY' AND next_at<=NOW(3)) OR (state='RUNNING' AND lease_until<NOW(3)))",token,id)==1;}
    @Override
    public void finishJob(long id,String token){jdbc.update("UPDATE lottery_v2_job SET state='DONE',lease_token=NULL,lease_until=NULL,last_error=NULL WHERE request_pk=? AND lease_token=?",id,token);}
    @Override
    public void retryJob(long id,String token,String message){jdbc.update("UPDATE lottery_v2_job SET state='READY',lease_token=NULL,lease_until=NULL,next_at=DATE_ADD(NOW(3),INTERVAL 10 SECOND),last_error=? WHERE request_pk=? AND lease_token=?",message==null?null:message.substring(0,Math.min(512,message.length())),id,token);}

    @Transactional(rollbackFor=Exception.class)
    @Override
    public void earn(long b,LotterySettingsCacheDataVO cfg,long m,String period,String scope,int source,int limit,boolean unlimited,int reward,int balance) {
        long a=cfg.getActivityId();
        var versions=jdbc.queryForList("SELECT config_version FROM lottery_settings WHERE id=? AND business_id=? AND state=1 AND deleted=0 LOCK IN SHARE MODE",cfg.getId(),b);
        if(versions.isEmpty()||n(versions.get(0),"config_version")!=cfg.getConfigVersion())throw exception(LOTTERY_SYSTEM_AGAIN);
        ensureCounter(b,a,m,"TOTAL",0); // 与受理时保持相同的会员计数锁顺序。
        jdbc.queryForObject("SELECT consumed FROM lottery_v2_counter WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key='TOTAL' AND source=0 FOR UPDATE",Long.class,b,a,m);
        var available=LotteryQuota.calculate(cfg,counters(b,a,m,period),period,balance,false);
        if(available.getSum()==0 || (available.getSum()>0 && (available.getNum()<0 || available.getNum()>=available.getSum())))
            throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
        ensureCounter(b,a,m,scope,source);
        if(jdbc.update("UPDATE lottery_v2_counter SET gained=gained+?,finished=finished+1 WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key=? AND source=? AND (?=1 OR finished<?)",reward,b,a,m,scope,source,unlimited?1:0,limit)!=1)
            throw exception(LOTTERY_TASK_LIMIT_REACHED);
        jdbc.update("UPDATE lottery_v2_counter SET revision=revision+1 WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key='TOTAL' AND source=0",b,a,m);
        quotaCache.changed(b,a,m);
    }
}
