package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
/** 仅补充抽奖发放状态，中奖记录及分片主体不改变。 */
@Service
public class LotteryRecordStateService {
    @Resource private JdbcTemplate jdbc;
    private JdbcTemplate readJdbc;
    @PostConstruct void initialize(){readJdbc=new JdbcTemplate(jdbc.getDataSource());readJdbc.setQueryTimeout(5);}
    private JdbcTemplate db(){return readJdbc==null?jdbc:readJdbc;}
    public void enrich(List<LotteryLogDO> logs) {
        if(logs==null||logs.isEmpty())return;
        long business=BusinessContextHolder.getRequiredBusinessId();
        logs.forEach(row->row.setCanReissue(false));
        for(int offset=0;offset<logs.size();offset+=200) {
            var batch=logs.subList(offset,Math.min(offset+200,logs.size()));
            var byId=batch.stream().collect(Collectors.toMap(LotteryLogDO::getId,x->x,(a,b)->a));
            String placeholders=String.join(",",Collections.nCopies(byId.size(),"?"));
            var args=new ArrayList<Object>();args.add(business);args.addAll(byId.keySet());
            for(var row:db().queryForList("SELECT r.id,r.member_id,r.request_id,r.grant_status,r.draw_status,j.state,j.last_error FROM lottery_v2_request r LEFT JOIN lottery_v2_job j ON j.request_pk=r.id WHERE r.business_id=? AND r.id IN ("+placeholders+")",args.toArray())) {
                var target=byId.get(((Number)row.get("id")).longValue());
                if(!Objects.equals(target.getMemberId(),((Number)row.get("member_id")).longValue()))continue;
                target.setRequestId((String)row.get("request_id"));target.setGrantStatus((String)row.get("grant_status"));
                target.setFailReason((String)row.get("last_error"));
                target.setCanReissue("STOPPED".equals(row.get("state"))&&!"COMPLETED".equals(row.get("draw_status"))&&!"RETURNED".equals(row.get("draw_status")));
            }
        }
    }
}
