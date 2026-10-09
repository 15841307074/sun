package com.htyoudao.youdao.module.member.api.wxmember;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.wxmember.dto.MemberOrderDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberBenefitJobDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDayDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberDataVO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "用户模块")
public interface WxMemberApi {
    /** 根据业务号幂等增减抽奖积分；返回 false 表示本次变更未成功。 */
    boolean changeLotteryPoints(Long memberId, String businessNo, int delta);

    /** 仅退回已成功扣除的抽奖积分，同一笔最多退回一次。 */
    void refundLotteryPoints(Long memberId, String drawBusinessNo);


    /**
     * 更新最后下单时间
     *
     * @param memberId
     * @return
     */
    CommonResult<MemberOrderDTO> updateFinalOrderFinishTime(@RequestParam("memberId") Long memberId);

    /**
     * 通过memberId查询会员信息
     *
     * @param memberId memberId
     * @return WxMemberDTO
     */
    CommonResult<WxMemberDTO> getMemberById(@RequestParam("memberId") Long memberId);

    /**
     * 通过memberId批量查询会员信息
     *
     * @param memberIds memberIds
     * @return WxMemberDTO
     */
    List<WxMemberDTO> getMemberByIds(List<Long> memberIds);

    /**
     * 通过memberId批量查询会员展示名称
     *
     * @param memberIds memberIds
     * @return memberId -> 会员展示名称
     */
    Map<Long, String> getMemberNameMapByIds(List<Long> memberIds);

    /**
     * 更新会员积分
     *
     * @param memberId      memberId
     * @param shardingValue shardingValue
     * @param productPrice  productPrice
     */
    void updateIntegral(Long memberId, Integer shardingValue, Long productPrice);

    /**
     * 通过memberMobile查询会员信息
     *
     * @param memberMobile memberMobile
     * @return WxMemberDTO
     */
    WxMemberDTO getMemberByMobile(String memberMobile);

    /**
     * 通过memberMobile查询会员信息
     *
     * @param memberMobiles memberMobiles
     * @return WxMemberDTO
     */
    List<WxMemberDTO> getMemberByMobiles(List<String> memberMobiles);

    /**
     * 根据用户id 查询用户
     */
    @GetMapping("/getWxMemberById")
    CommonResult<WxMemberVO> getWxMemberById(@RequestParam("memberId") Long memberId);

    /**
     * 抽奖修改用户积分
     */
    @PutMapping("/updateMemberById")
    void updateMemberById(@RequestParam("memberId") Long memberId, @RequestParam("memberIntegral") int memberIntegral);

    /**
     * 全部微信会员列表
     * @return
     */
    @GetMapping("/listWechat")
    public CommonResult<List<WxMemberDataVO>> listWechat(long current,int size);

    /**
     * 根据会员等级查询会员
     * @param memberLevel memberLevel
     * @return List<WxMemberDTO>
     */
    CommonResult<List<WxMemberDTO>> getMemberByMemberLevel(Integer memberLevel);

    /**
     * 根据会员等级查询会员
     * @param key key
     * @param value value
     * @return Set<String>
     */
    Set<String> getSmsPhone(Integer key, List<Long> value);

    /**
     * 获取所有会员
     * @return List<WxMemberDTO>
     */
    List<WxMemberDTO> getMembers();


    /**
     * 获取会员日的会员
     * @param shardingValue shardingValue
     * @return List<WxMemberDayDTO>
     */
    List<WxMemberDayDTO> getDayMembers(int shardingValue);

    Map<Integer, List<WxMemberDTO>> getMembersMapByShard(int shardingValue,Long lastCursorId,int pageSize);

    /**
     * 会员卡权益每日发放任务专用：按分片和 member_id 游标查询有效会员。
     */
    List<WxMemberBenefitJobDTO> listMemberCardBenefitJobMembers(Long businessId, int shardingValue, Long lastCursorId, int pageSize);

    /**
     * 会员卡权益每日发放任务专用：命中升级后更新会员等级。
     */
    void updateMemberLevel(Long businessId, Long memberId, Integer shardingValue, Integer memberLevel);

    /**
     * 更新是否是跑腿员
     * @param memberId memberId
     * @param errandFlag errandFlag
     */
    void updateErrandFlag(Long memberId,Integer shardingValue,Boolean errandFlag);


    /**
     * 验证交易密码。
     *
     * @param memberId 会员 ID
     * @param tranPassword 交易密码
     * @return 是否验证通过
     */
    boolean verifyTranPassword(Long memberId, String tranPassword);

}
