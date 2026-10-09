package com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.impl;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.crowd.CrowdApi;
import com.htyoudao.youdao.module.member.api.wecom.WecomGroupApi;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckAnyMemberReqVO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.ClaimCouponService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author duht
 * 领取优惠券 第6个被调用的 impl
 * 判断新老用户关系
 */
@Slf4j
@Service("claimCouponRestrict")
public class ClaimCouponRestrictImpl implements ClaimCouponService {

    /**
     * 下一个impl 判断领取到没到上限
     */
    @Resource
    @Qualifier("claimCouponLimit")
    private ClaimCouponService claimCouponService;
    @DubboReference
    private CrowdApi crowdApi;

    @DubboReference
    private WecomGroupApi wecomGroupApi;

    /**
     * 判断是否是新老用户
     * @param goodCoupon 优惠券
     * @param wxMember 微信会员
     */
    @Override
    public void claimOneCoupon(GoodCouponRespVO goodCoupon, WxMemberDTO wxMember) {
        Date registerTime = wxMember.getRegisterTime();
        Integer userRestrictions = goodCoupon.getUserRestrictions();
        Date lastLoginTime = wxMember.getLastLoginTime();
        log.info("进入第六个impl，判断新老用户关系{},{},{}", userRestrictions, registerTime,lastLoginTime);
        // 新注册用户
        if(userRestrictions.equals(1)){
            boolean result = userTypeSevenDay(wxMember);
            if (!result){
                throw exception(ErrorCodeConstants.COUPON_NOT_REGISTER_NEW);
            }
        }
        //老用户
        if(userRestrictions.equals(2)){
            boolean result = userType(registerTime);
            if (!result) {
                throw exception(ErrorCodeConstants.COUPON_NOT_REGISTER_OLD);
            }
        }
        //回归用户
        if(userRestrictions.equals(3)){
            boolean result = userTypeThirtyDay(wxMember);
            if (!result) {
                throw exception(ErrorCodeConstants.COUPON_NOT_REGISTER_BACK);
            }
        }
        // 人群
        if(userRestrictions.equals(4)){
            boolean result = crowdApi.memberExist(wxMember.getMemberId(),goodCoupon.getVersion());
            if (!result) {
                throw exception(ErrorCodeConstants.COUPON_NOT_CROWD);
            }
        }
        // 需要进社群
        if(Objects.equals(1,goodCoupon.getCommunityFlag())){
            WecomGroupCheckAnyMemberReqVO reqVO = new WecomGroupCheckAnyMemberReqVO();
            reqVO.setUnionId(wxMember.getWxUnionid());
            CommonResult<Boolean> userInAnyGroup = wecomGroupApi.isUserInAnyGroup(reqVO);
            Boolean data = userInAnyGroup.getData();
            if (!data) {
                throw exception(ErrorCodeConstants.BASE_ACTIVITY_NOT_IN_GROUP);
            }
        }
        claimCouponService.claimOneCoupon(goodCoupon, wxMember);
    }

    /**
     * 新用户判断
     * @param wxMember wxMember
     * @return boolean
     */
    public boolean userTypeSevenDay(WxMemberDTO wxMember) {
        // 获取当前时间
        Date date = new Date();
        Date registerTime = wxMember.getRegisterTime();
        if (ObjectUtil.isEmpty(registerTime)) {
            return true;
        }
        long diffInMillis = Math.abs(date.getTime() - registerTime.getTime());
        // 获取两个日期时间的毫秒差值，取绝对值避免顺序影响结果
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        return diffInDays <= 7;
    }

    /**
     * 老 用户判断
     * @param userDate date
     * @param userDate 天数
     * @return boolean
     */
    public boolean userType(Date userDate) {
        // 获取当前时间
        if(ObjectUtil.isEmpty(userDate)){
            return false;
        }
        Date date = new Date();
        long diffInMillis = Math.abs(date.getTime() - userDate.getTime());
        // 获取两个日期时间的毫秒差值，取绝对值避免顺序影响结果
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        return diffInDays > 7;
    }

    /**
     * 回归用户判断
     * @param wxMember wxMember
     * @return boolean
     */
    public boolean userTypeThirtyDay(WxMemberDTO wxMember) {
        Date finalOrderFinishTime = wxMember.getFinalOrderFinishTime();
        Date registerTime = wxMember.getRegisterTime();
        log.info("判断是否是回归用户,{},{}",finalOrderFinishTime,registerTime);
        if(ObjectUtil.isEmpty(finalOrderFinishTime) || ObjectUtil.isEmpty(registerTime)){
            return false;
        }
        // 获取当前时间
        Date date = new Date();
        long diffInMillis = Math.abs(date.getTime() - finalOrderFinishTime.getTime());
        long registerMillis = Math.abs(date.getTime() - registerTime.getTime());
        // 获取两个日期时间的毫秒差值，取绝对值避免顺序影响结果
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        long registerInDays = registerMillis / (1000 * 60 * 60 * 24);
        return diffInDays > 14 && registerInDays > 7;
    }
}
