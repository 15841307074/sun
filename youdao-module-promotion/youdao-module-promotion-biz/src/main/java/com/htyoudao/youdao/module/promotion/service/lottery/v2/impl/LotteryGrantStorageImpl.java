package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/** 固定业务编号让重试落到同一张优惠券或抽奖记录。 */
@Service("lotteryGrantStorage")
@DS("sharding")
public class LotteryGrantStorageImpl implements LotteryGrantStorage {
    @Resource
    private UserCouponMapper coupons;
    @Resource
    private LotteryLogMapper logs;
    @Resource
    private LotteryWinnerFeed winnerFeed;
    @Resource private LotteryLedger ledger;
    @Override
    @Transactional(rollbackFor=Exception.class,timeout=5)
    public boolean claimPhysicalReturn(LotteryLedger.Draw draw) {
        var key=new LambdaQueryWrapper<LotteryLogDO>().eq(LotteryLogDO::getBusinessId,draw.getBusinessId())
                .eq(LotteryLogDO::getMemberId,draw.getMemberId()).eq(LotteryLogDO::getId,draw.getId());
        LotteryLogDO row=logs.selectOne(key);
        if(row==null)return false;
        if(Objects.equals(row.getPrizeState(),9))return true;
        if(!Objects.equals(row.getPrizeType(),3)||!Objects.equals(row.getPrizeState(),1)
                ||row.getCreateTime().isAfter(LocalDate.now().minusDays(2).atStartOfDay()))return false;
        LotteryLogDO update=new LotteryLogDO();update.setPrizeState(9);
        return logs.update(update,key.eq(LotteryLogDO::getPrizeState,1))==1;
    }
    @Override
    @Transactional(rollbackFor=Exception.class,timeout=5)
    public void coupon(LotteryLedger.Draw draw) {
        var s=draw.getSnapshot();var template=s.getCoupon();
        if(template==null)throw new IllegalStateException("缺少发券快照");
        UserCouponDO row=BeanUtils.toBean(template,UserCouponDO.class);
        row.setId(draw.getId());row.setUserId(draw.getMemberId());row.setCouponId(s.getPrize().getAwardId());row.setIsUsed(0);
        row.setUseTime(null);row.setBusinessId(draw.getBusinessId());row.setMemberMobile(s.getMember().getMemberMobile());
        row.setCouponSource(CouponSourceType.PRIZE_DRAW.getCode());
        LocalDateTime accepted=LocalDateTime.ofInstant(Instant.ofEpochMilli(s.getAcceptedAt()),ZoneId.systemDefault());
        row.setCreateTime(accepted);row.setUpdateTime(accepted);
        LocalDate day=accepted.toLocalDate();
        if(Objects.equals(template.getUseType(),0)){row.setVaildStartTime(template.getCouponStartTime());row.setExpirationTime(template.getCouponEndTime());}
        else if(Objects.equals(template.getUseType(),1)) {row.setVaildStartTime(new Date(s.getAcceptedAt()));row.setExpirationTime(end(day.plusDays(Integer.parseInt(template.getUseTime())-1)));}
        else if(Objects.equals(template.getUseType(),2)) {
            String[] parts=template.getUseTime().split("#");LocalDate start=day.plusDays(Integer.parseInt(parts[0]));
            row.setVaildStartTime(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));row.setExpirationTime(end(start.plusDays(Integer.parseInt(parts[1])-1)));
        }
        try{coupons.insert(row);}catch(DuplicateKeyException e){
            UserCouponDO previous=coupons.selectOne(new LambdaQueryWrapper<UserCouponDO>().eq(UserCouponDO::getId,draw.getId()).eq(UserCouponDO::getUserId,draw.getMemberId()));
            if(previous==null||!Objects.equals(previous.getCouponId(),row.getCouponId())||!Objects.equals(previous.getBusinessId(),draw.getBusinessId()))throw e;
        }
    }
    @Override
    public void log(LotteryLedger.Draw draw) {
        var s=draw.getSnapshot();var prize=s.getPrize();var cfg=s.getSettings();
        LotteryLogDO row=new LotteryLogDO();row.setId(draw.getId());row.setBusinessId(draw.getBusinessId());
        row.setLotteryPrizeId(prize.getId());row.setLotteryId(cfg.getId());row.setMemberId(draw.getMemberId());row.setStoreId(draw.getStoreId());
        row.setMemberMobile(s.getMember().getMemberMobile());row.setMemberName(s.getMember().getMemberNickName());
        row.setPrizeName(prize.getPrizeName());row.setPrizeType(prize.getPrizeType());row.setPrizeImgUrl(prize.getPrizeImgUrl());row.setPrizeValue(prize.getPrizeValue());
        row.setIsGuarantees(prize.getIsGuarantees());
        row.setPrice(draw.getPointsCost());row.setLotteryType(cfg.getLotteryType());row.setLotteryStartTime(cfg.getLotteryStartTime());row.setLotteryEndTime(cfg.getLotteryEndTime());
        row.setPrizeState(Objects.equals(prize.getPrizeType(),3)?1:0);row.setClaimStatus(Objects.equals(prize.getPrizeType(),5)&&"SUCCESS".equals(draw.getGrantStatus())?2:1);
        row.setCreateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(s.getAcceptedAt()),ZoneId.systemDefault()));row.setUpdateTime(row.getCreateTime());
        if(Objects.equals(prize.getPrizeType(),5)){
            if(draw.getOutBillNo()!=null&&!draw.getOutBillNo().equals("L"+draw.getId()))row.setOutBillNo(draw.cashBillNo());
            if(draw.getResult()!=null)row.setPackageInfo(draw.getResult().getPackageInfo());
        }
        try{logs.insert(row);}catch(DuplicateKeyException e){
            LotteryLogDO previous=logs.selectOne(new LambdaQueryWrapper<LotteryLogDO>().eq(LotteryLogDO::getId,draw.getId()).eq(LotteryLogDO::getMemberId,draw.getMemberId()));
            if(previous==null||!Objects.equals(previous.getBusinessId(),draw.getBusinessId()))throw e;
            if(Objects.equals(prize.getPrizeType(),5)||Objects.equals(previous.getPrizeType(),5)) {
                // 只回写当前版本的红包参数；迟到的旧发奖/补发不能覆盖新单号或兜底奖品。
                LotteryLedger.Draw latest=ledger.get(draw.getId());
                if(latest==null||latest.getSnapshot().getReissueSequence()!=s.getReissueSequence()
                        ||!Objects.equals(latest.getSnapshot().getPrize().getId(),prize.getId())||!Objects.equals(latest.cashBillNo(),draw.cashBillNo()))return;
                if(Objects.equals(prize.getPrizeType(),5)) {
                    row.setClaimStatus("SUCCESS".equals(latest.getGrantStatus())?2:1);
                    row.setPackageInfo(latest.getResult()==null?null:latest.getResult().getPackageInfo());
                }
                var update=new LambdaUpdateWrapper<LotteryLogDO>().eq(LotteryLogDO::getId,draw.getId())
                        .eq(LotteryLogDO::getMemberId,draw.getMemberId()).eq(LotteryLogDO::getBusinessId,draw.getBusinessId());
                if(previous.getOutBillNo()==null)update.isNull(LotteryLogDO::getOutBillNo);
                else update.eq(LotteryLogDO::getOutBillNo,previous.getOutBillNo());
                update.set(LotteryLogDO::getOutBillNo,row.getOutBillNo()).set(LotteryLogDO::getPackageInfo,row.getPackageInfo());
                if(logs.update(row,update)!=1)throw new IllegalStateException("红包抽奖记录发生并发更新，需按当前流水重试");
            }
        }
        if("COMPLETED".equals(draw.getDrawStatus()))winnerFeed.afterLogSaved(draw.getActivityId(), row);
    }
    private Date end(LocalDate day){return Date.from(day.atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant());}
}
