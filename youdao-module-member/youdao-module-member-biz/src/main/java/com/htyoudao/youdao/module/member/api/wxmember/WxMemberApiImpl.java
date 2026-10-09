package com.htyoudao.youdao.module.member.api.wxmember;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.member.api.ApiConstants;
import com.htyoudao.youdao.module.member.api.wxmember.dto.MemberOrderDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberBenefitJobDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDayDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberDataVO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.service.wxmember.LotteryPointsService;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;

@DubboService(timeout = 50000)
@Slf4j
public class WxMemberApiImpl implements WxMemberApi{
    @Resource
    private LotteryPointsService lotteryPointsService;

    @Override
    public void refundLotteryPoints(Long memberId, String drawBusinessNo) {
        lotteryPointsService.refund(memberId, drawBusinessNo);
    }

    @Override
    public boolean changeLotteryPoints(Long memberId, String businessNo, int delta) {
        return lotteryPointsService.change(memberId, businessNo, delta);
    }

    @Resource
    private WxMemberService wxMemberService;

    @Override
    public CommonResult<MemberOrderDTO> updateFinalOrderFinishTime(Long memberId) {
        return CommonResult.success(wxMemberService.updateFinalOrderFinishTime(memberId));
    }
    /**
     *根据用户id 查询用户
     */
    @Override
    public CommonResult<WxMemberVO> getWxMemberById(@RequestParam("memberId") Long memberId) {
        WxMemberDO wxMemberDO =   wxMemberService.getWxMember(memberId);
        return CommonResult.success(BeanUtils.toBean(wxMemberDO, WxMemberVO.class));
    }
    /**
     * 抽奖修改用户积分
     */
    /**
     *抽奖修改用户积分
     */
    @Override
    public void updateMemberById(@RequestParam("memberId") Long memberId,@RequestParam("memberIntegral") int memberIntegral) {
        wxMemberService.updateMemberById(memberId,memberIntegral);
    }

    @Override
    public CommonResult<List<WxMemberDataVO>> listWechat(long current,int size) {
        return wxMemberService.listWechat(current,size);
    }

    @SentinelResource(value = "getMemberById", fallback = "getMemberByIdFallback", blockHandler = "getMemberByIdExceptionHandler")
    @Override
    public CommonResult<WxMemberDTO> getMemberById(Long memberId) {
        return wxMemberService.getMemberById(memberId);
    }

    @Override
    public List<WxMemberDTO> getMemberByIds(List<Long> memberIds) {
        return wxMemberService.getMemberByIds(memberIds);
    }

    @Override
    public Map<Long, String> getMemberNameMapByIds(List<Long> memberIds) {
        return wxMemberService.getMemberNameMapByIds(memberIds);
    }

    public CommonResult<WxMemberDTO> getMemberByIdFallback(Long memberId, Throwable ex) {
        log.error("WxMemberApiImpl.getMemberByIdFallback", ex);
        throw exception(WX_MEMBER_FALLBACK_ERROR);
    }

    public CommonResult<WxMemberDTO> getMemberByIdExceptionHandler(Long memberId, BlockException ex) {
        log.error("WxMemberApiImpl.getMemberByIdFallback", ex);
        throw exception(WX_MEMBER_BLOCK_ERROR);
    }

    @Override
    public void updateIntegral(Long memberId, Integer shardingValue, Long productPrice) {
        wxMemberService.updateIntegral(memberId,  shardingValue, productPrice);
    }

    @Override
    public WxMemberDTO getMemberByMobile(String memberMobile) {
        return wxMemberService.getMemberByMobile(memberMobile);
    }

    @Override
    public List<WxMemberDTO> getMemberByMobiles(List<String> memberMobiles) {
        return wxMemberService.getMemberByMobiles(memberMobiles);
    }

    @Override
    public CommonResult<List<WxMemberDTO>> getMemberByMemberLevel(Integer memberLevel) {
        return CommonResult.success(wxMemberService.getMemberByMemberLevel(memberLevel));
    }

    @Override
    public Set<String> getSmsPhone(Integer key, List<Long> value) {
        return wxMemberService.getSmsPhone(key,value);
    }

    @Override
    public List<WxMemberDTO> getMembers() {
        return wxMemberService.getMembers();
    }


    @Override
    public List<WxMemberDayDTO> getDayMembers(int shardingValue) {
        return wxMemberService.getDayMembers(shardingValue);
    }
    @Override
    public Map<Integer, List<WxMemberDTO>> getMembersMapByShard(int shardingValue,Long lastCursorId,int pageSize){

        return wxMemberService.getMembersMapByShard(  shardingValue,  lastCursorId,  pageSize);
    }

    @Override
    public List<WxMemberBenefitJobDTO> listMemberCardBenefitJobMembers(Long businessId, int shardingValue, Long lastCursorId, int pageSize) {
        return wxMemberService.listMemberCardBenefitJobMembers(businessId, shardingValue, lastCursorId, pageSize);
    }

    @Override
    public void updateMemberLevel(Long businessId, Long memberId, Integer shardingValue, Integer memberLevel) {
        wxMemberService.updateMemberLevel(businessId, memberId, shardingValue, memberLevel);
    }

    @Override
    public void updateErrandFlag(Long memberId, Integer shardingValue, Boolean errandFlag) {
        wxMemberService.updateErrandFlag(memberId,shardingValue,errandFlag);
    }

    /**
     * 验证交易密码
     */
    @Override
    public boolean verifyTranPassword(Long memberId, String tranPassword) {
        return wxMemberService.verifyTranPassword(memberId,tranPassword);
    }

}
