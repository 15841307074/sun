package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponpackage.CouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcouponpackage.GoodCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/** 抽奖券包独立发放，不调用无幂等键的通用领券包方法。 */
@Service
public class LotteryCouponPackageService {
    @Resource private CouponPackageMapper packages;
    @Resource private GoodCouponPackageMapper relations;
    @Resource private GoodCouponMapper coupons;
    @Resource private LotteryCouponPackageStorage storage;
    @Resource private JdbcTemplate jdbc;
    @Resource private ObjectMapper json;
    private JdbcTemplate grantJdbc;
    private static final ZoneId ZONE=ZoneId.of("Asia/Shanghai");
    @PostConstruct void initialize(){grantJdbc=new JdbcTemplate(jdbc.getDataSource());grantJdbc.setQueryTimeout(5);}
    private JdbcTemplate db(){return grantJdbc==null?jdbc:grantJdbc;}
    public LotteryDrawSnapshot.PackageReward snapshot(Long id) {
        if(id==null)throw com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryErrors.invalid("请选择优惠券包");
        var pack=packages.selectById(id);
        long business=BusinessContextHolder.getRequiredBusinessId();
        if(pack==null||!Objects.equals(pack.getBusinessId(),business)||Objects.equals(pack.getIsGround(),0))
            throw com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryErrors.invalid("优惠券包不存在或已下架");
        var links=relations.selectList(new LambdaQueryWrapper<GoodCouponPackageDO>().eq(GoodCouponPackageDO::getPackageId,id));
        if(links.isEmpty())throw com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryErrors.invalid("优惠券包内没有优惠券");
        var result=new LotteryDrawSnapshot.PackageReward();result.setPackageId(id);
        for(var link:links) {
            var coupon=coupons.selectById(link.getCouponId());
            if(coupon==null||!Objects.equals(coupon.getBusinessId(),business)||link.getNum()==null||link.getNum()<=0)
                throw com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryErrors.invalid("优惠券包明细无效");
            if((long)result.getCoupons().size()+link.getNum()>1000)throw com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryErrors.invalid("单个抽奖券包最多发放1000张券");
            for(int i=0;i<link.getNum();i++) {
                var copy=new LotteryDrawSnapshot.CouponCopy();copy.setId(IdWorker.getId());copy.setTemplate(coupon);result.getCoupons().add(copy);
            }
        }
        return result;
    }
    @Data public static class Plan {
        private UserCouponPackageDO header;
        private List<UserCouponDO> coupons=new ArrayList<>();
    }
    public void grant(LotteryLedger.Draw draw) {
        var reward=draw.getSnapshot().getCouponPackage();
        if(reward==null||reward.getCoupons()==null||reward.getCoupons().isEmpty())throw new IllegalStateException("缺少中奖券包快照");
        var found=db().queryForList("SELECT plan_json FROM lottery_coupon_package_grant WHERE business_id=? AND request_pk=?",String.class,draw.getBusinessId(),draw.getId());
        Plan plan;
        try {
            if(found.isEmpty()) {
                Plan prepared=buildPlan(draw,reward,LocalDateTime.now(ZONE));
                db().update("INSERT IGNORE INTO lottery_coupon_package_grant(business_id,request_pk,plan_json) VALUES(?,?,?)",draw.getBusinessId(),draw.getId(),json.writeValueAsString(prepared));
                String saved=db().queryForObject("SELECT plan_json FROM lottery_coupon_package_grant WHERE business_id=? AND request_pk=?",String.class,draw.getBusinessId(),draw.getId());
                plan=json.readValue(saved,Plan.class);
            } else plan=json.readValue(found.get(0),Plan.class);
        }catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException("券包发放快照解析失败",e);}
        // 固定ID逐条落库，提交结果不确定时也只核对同一张券；不跨库持有大事务。
        for(var coupon:plan.getCoupons())storage.coupon(coupon);
        storage.header(plan.getHeader());
        db().update("UPDATE lottery_coupon_package_grant SET state='SUCCESS',update_time=NOW() WHERE business_id=? AND request_pk=?",draw.getBusinessId(),draw.getId());
    }
    private Plan buildPlan(LotteryLedger.Draw draw,LotteryDrawSnapshot.PackageReward reward,LocalDateTime issuedAt) {
        Plan plan=new Plan();
        var header=new UserCouponPackageDO();header.setId(draw.getId());header.setBusinessId(draw.getBusinessId());header.setUserId(draw.getMemberId());
        header.setPackageId(reward.getPackageId());header.setPackageSource(CouponSourceType.PRIZE_DRAW.getCode());
        header.setMemberMobile(draw.getSnapshot().getMember().getMemberMobile());header.setMemberNickName(draw.getSnapshot().getMember().getMemberNickName());
        header.setCreateTime(issuedAt);header.setUpdateTime(issuedAt);header.setDeleted(false);plan.setHeader(header);
        for(var copy:reward.getCoupons()) {
            var template=copy.getTemplate();var row=BeanUtils.toBean(template,UserCouponDO.class);
            row.setId(copy.getId());row.setBusinessId(draw.getBusinessId());row.setUserId(draw.getMemberId());row.setCouponId(template.getId());
            row.setPackageId(reward.getPackageId());row.setMemberMobile(header.getMemberMobile());row.setCouponSource(CouponSourceType.PRIZE_DRAW.getCode());
            row.setStoreId(draw.getStoreId());row.setIsUsed(0);row.setUseTime(null);row.setDeleted(false);row.setDistributionMethod(0L);
            row.setCouponUseTime(template.getUseTime());row.setCreateTime(issuedAt);row.setUpdateTime(issuedAt);row.setCouponCreateTime(issuedAt);
            var start=issuedAt;var day=issuedAt.toLocalDate();LocalDateTime end;
            int type=template.getUseType();String rule=template.getUseTime();
            if(type==0){String[] parts=rule.split("#");start=LocalDate.parse(parts[0]).atStartOfDay();end=LocalDate.parse(parts[1]).atTime(23,59,59);}
            else if(type==1)end=day.plusDays(Integer.parseInt(rule)-1).atTime(23,59,59);
            else if(type==2){String[] parts=rule.split("#");var first=day.plusDays(Integer.parseInt(parts[0]));start=first.atStartOfDay();end=first.plusDays(Integer.parseInt(parts[1])-1).atTime(23,59,59);}
            else if(type==3)end=day.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(Integer.parseInt(rule)))).atTime(23,59,59);
            else throw com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryErrors.invalid("券包优惠券有效期类型不支持");
            row.setVaildStartTime(Date.from(start.atZone(ZONE).toInstant()));row.setExpirationTime(Date.from(end.atZone(ZONE).toInstant()));plan.getCoupons().add(row);
        }
        return plan;
    }
}
