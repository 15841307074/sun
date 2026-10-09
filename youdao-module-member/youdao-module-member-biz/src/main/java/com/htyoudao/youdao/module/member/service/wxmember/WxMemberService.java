package com.htyoudao.youdao.module.member.service.wxmember;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.api.wxmember.dto.MemberOrderDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberCrowdDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberBenefitJobDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDayDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberDataVO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRespVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberSendReqVO;
import com.htyoudao.youdao.module.member.controller.app.wxmember.vo.*;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberAndCardReqVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberAndCardRespVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberCardDataRespVO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CustomCrowdDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 会员 Service 接口
 *
 * @author 零零玖零
 */
public interface WxMemberService {

    /**
     * 删除会员
     *
     * @param id 编号
     */
    void deleteWxMember(Long id);

    /**
     * 获得会员
     *
     * @param id 编号
     * @return 会员
     */
    WxMemberDO getWxMember(Long id);


    /**
     * 创建wx成员
     *
     * @param wxMember wx成员
     * @return {@link Long }
     */
    Long createWxMemberByMiniProgram(WxMemberDO wxMember);

    /**
     * 查询会员列表
     *
     * @param wxMember wx成员
     * @return {@link List }<{@link WxMemberDO }>
     */
    List<WxMemberDO> selectList(WxMemberDO wxMember);


    /**
     * 更新最后一次下单时间
     *
     * @param memberId memberId
     * @return {@link Long }
     */
    MemberOrderDTO updateFinalOrderFinishTime(Long memberId);

    /**
     * 根据会员id获取会员信息
     * @param memberId memberId
     * @return WxMemberDTO
     */
    CommonResult<WxMemberDTO> getMemberById(Long memberId);

    /**
     * 根据会员id批量获取会员信息
     * @param memberIds memberIds
     * @return WxMemberDTO
     */
    List<WxMemberDTO> getMemberByIds(List<Long> memberIds);

    /**
     * 根据会员id批量获取会员展示名称
     * @param memberIds memberIds
     * @return memberId -> 会员展示名称
     */
    Map<Long, String> getMemberNameMapByIds(List<Long> memberIds);

    /**
     * 更新会员积分
     * @param memberId memberId
     * @param shardingValue shardingValue
     * @param productPrice productPrice
     */
    void updateIntegral(Long memberId, Integer shardingValue, Long productPrice);

    /**
     * 根据会员手机号获取会员信息
     * @param memberMobile memberMobile
     * @return WxMemberDTO
     */
    WxMemberDTO getMemberByMobile(String memberMobile);

    /**
     * 根据会员手机号批量获取会员信息
     * @param memberMobiles memberMobiles
     * @return WxMemberDTO
     */
    List<WxMemberDTO> getMemberByMobiles(List<String> memberMobiles);

    /**
     * 会员pc端查询
     * @param wxMemberReqVO wxMemberReqVO
     * @return WxMemberRespVO
     */
    PageResult<WxMemberRespVO> selectListPage(WxMemberReqVO wxMemberReqVO);

    /**
     * 会员pc端导出
     * @param wxMemberReqVO wxMemberReqVO
     * @param request request
     * @param response response
     */
    void exportMemberList(WxMemberReqVO wxMemberReqVO, HttpServletRequest request, HttpServletResponse response);
    /**
     * 更新会员积分
     * @param memberId
     * @param memberIntegral
     */
    void updateMemberById(Long memberId, int memberIntegral);

    /**
     * 更新会员最后登录时间
     * @param wxMember wxMember
     */
    void updateLastLoginTime(WxMemberUpdateLoginTimeReqVO wxMember);

    /**
     * 查询会员信息
     * @param wxMember wxMember
     * @return WxMemberInfoRespVO
     */
    WxMemberInfoRespVO listByEntity(WxMemberInfoReqVO wxMember);

    /**
     * 修改是否是第一次登录
     * @param wxMember wxMember
     */
    void updateFirstLogin(WxMemberUpdateFirstLoginReqVO wxMember);

    /**
     * 获取30天、365天、180天、7天订单数、总金额、平均订单金额
     * @return Boolean
     */
    Boolean updateHistoryOrderData();

    /**
     * 更新会员标签
     * @return Boolean
     */
    Boolean updateMemberLabel();

    /**
     * 获取会员首单店铺id
     */
    void updateFirstOrderStoreId();

    void updatePointBatch(Long businessId,List<WxMemberDO> memberList);

    WxMemberDO selectById(Long memberId);

    void updateMemberByJustId(Long memberId, WxMemberDO existingMember);

    CommonResult<List<WxMemberDataVO>> listWechat(long current,int size);

    WxMemberAndCardRespVO getMemberCardWithMemberId(WxMemberAndCardReqVO reqVO);

    /**
     * 根据会员等级查询会员信息
     * @param memberLevel memberLevel
     * @return CommonResult<List<WxMemberDTO>>
     */
    List<WxMemberDTO> getMemberByMemberLevel(Integer memberLevel);

    /**
     * 保存订阅消息的会员信息
     * @param reqVO reqVO
     * @return Boolean
     */
    Boolean saveNoticeReserveMember(NoticeReserveMemberReqVO reqVO);

    /**
     * 获取会员手机号
     * @param key key
     * @param value value
     * @return Set<String>
     */
    Set<String> getSmsPhone(Integer key, List<Long> value);


    WxMemberDO selectByIdAndBusinessId(Long memberId, String businessId);

    void updateMemberByJustIdAndBusinessId(Long memberId, String businessId, WxMemberDO existingMember);


    /**
     * 获取所有会员
     * @return List<WxMemberDTO>
     */
    List<WxMemberDTO> getMembers();

    /**
     * 获取会员日的会员
     * @param shardingValue shardingValue
     * @return List<WxMemberDTO>
     */
    List<WxMemberDayDTO> getDayMembers(int shardingValue);

    /**
     * 发送优惠券
     * @param wxMemberSendReqVO wxMemberSendReqVO
     * @return Boolean
     */
    Boolean sendCoupon(WxMemberSendReqVO wxMemberSendReqVO);

    /**
     * 根据人群表基础信息与回购周期，末次距今下单时间获取满足要求的会员ID
     * @param customCrowdDO
     * @return
     */
    List<Long> getMemberWithCrowd(CustomCrowdDO customCrowdDO);

    List<Long> getMemberIdByMemberWarpper(LambdaQueryWrapper<WxMemberDO> memberWrapper);

    /**
     * 营销短信通过人群查人
     * @param crowdId crowdId
     * @return Boolean
     */
    List<WxMemberCrowdDTO> getMemberDataByCrowdId(String crowdId);

    List<Long> getAllMemberId(Long busId);

    Map<Integer, List<WxMemberDTO>> getMembersMapByShard(int shardingValue,Long lastCursorId,int pageSize);

    List<WxMemberBenefitJobDTO> listMemberCardBenefitJobMembers(Long businessId, int shardingValue, Long lastCursorId, int pageSize);

    void updateMemberLevel(Long businessId, Long memberId, Integer shardingValue, Integer memberLevel);

    List<Long> getCommunityList(String busId);

    List<Long> getNotInCommunityList(String busId);

    void resetCommunityFlag(int i);

    void setCommunityFlag(int i);

    /**
     * 更新是否是跑腿员
     * @param memberId memberId
     * @param errandFlag errandFlag
     */
    void updateErrandFlag(Long memberId,Integer shardingValue,Boolean errandFlag);


    /**
     * 设置交易密码。
     *
     * @param memberId 会员 ID
     * @param tranPassword 交易密码
     * @param confirmPassword 确认密码
     * @return 是否成功
     */
    boolean setTranPassword(Long memberId, String tranPassword, String confirmPassword);

    /**
     * 验证交易密码。
     *
     * @param memberId 会员 ID
     * @param tranPassword 交易密码
     * @return 是否验证通过
     */
    boolean verifyTranPassword(Long memberId, String tranPassword);

    /**
     * 检查是否已设置交易密码。
     *
     * @param memberId 会员 ID
     * @return 是否已设置
     */
    boolean isTranPasswordSet(Long memberId);

    /**
     * 找回交易密码。
     *
     * @param mobile 手机号
     * @param code 验证码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @param ip IP 地址
     * @return 是否成功
     */
    boolean forgetPassword(String mobile, String code, String newPassword,
                           String confirmPassword, String ip);

    /**
     * 修改交易密码。
     *
     * @param memberId 会员 ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @return 是否成功
     */
    boolean updatePassword(Long memberId, String oldPassword,
                           String newPassword, String confirmPassword);

    /**
     * 验证原密码。
     *
     * @param memberId 会员 ID
     * @param oldPassword 原密码
     * @return 是否成功
     */
    boolean checkTranOldPassword(Long memberId, String oldPassword);
}
