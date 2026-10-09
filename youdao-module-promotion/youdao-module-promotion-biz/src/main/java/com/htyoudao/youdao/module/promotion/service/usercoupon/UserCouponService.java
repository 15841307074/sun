package com.htyoudao.youdao.module.promotion.service.usercoupon;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.api.DTO.ItemDto;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.*;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponCountRespVo;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponPageReqVo;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponPageRespVo;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;

import java.util.List;
import java.util.Set;

/**
 * 用户优惠券 Service 接口
 *
 * @author dht
 */
public interface UserCouponService {

    /**
     * 我的优惠券数量
     *
     * @param userId       userId
     * @param isUsed       isUsed
     * @return Long
     */
    Long getCountNum(Long userId, Integer isUsed);

    /**
     * 查询我的优惠券列表
     *
     * @param appCouponListReqVO appCouponListReqVO
     * @return AppCouponListRespVO
     */
    PageResult<AppCouponListRespVO> selectUserCouponListByUserId(AppCouponListReqVO appCouponListReqVO);

    /**
     * 根据优惠券id、用户id 查询优惠券信息
     *
     * @param userCouponReqVO userCouponReqVO
     * @return UserCouponRespVO
     */
    UserOneCouponRespVO getCouponByUserAndCouponId(UserCouponReqVO userCouponReqVO);

    /**
     * 删除过期优惠券
     */
    void delOverdueCoupon();

    /**
     * 使用/回退优惠券
     *
     * @param usedCouponReqVO usedCouponReqVO
     */
    void usedCoupon(UsedCouponReqVO usedCouponReqVO);

    /**
     * 修改用户优惠券
     *
     * @param updateUserCouponReqVO 用户优惠券
     * @return 结果
     */
    @Deprecated
    void updateUserCoupon(UpdateUserCouponReqVO updateUserCouponReqVO);

    /**
     * 结算时查询用户优惠券列表
     *
     * @param settlementReqVO settlementReqVO
     * @return AppUserCouponRespVO
     */
    AppUserCouponRespVO getCouponList(SettlementReqVO settlementReqVO);

    /**
     * 获取优惠金额，单张优惠券
     *
     * @param settlementReqVO settlementReqVO
     * @return GetReduceAmountRspVO
     */
    CouponCalculateRespVO getReduceAmount(GetReduceAmountReqVO settlementReqVO);

    /**
     * 领取优惠券（分享页面）
     *
     * @param claimCoupon claimCoupon
     * @return Boolean
     */
    Boolean claimOneCoupon(ClaimCouponReqVO claimCoupon);

    /**
     * 在积分商品页面领取优惠券
     *
     * @param claimCoupon claimCoupon
     * @return Boolean
     */
    Boolean claimCouponWithProduct(ClaimCouponReqVO claimCoupon);

    Boolean insertBatch(List<UserCouponDO> userCoupons);

    List<UserCouponVO> selectCouponData(Long memberId);

    /**
     * 添加优惠卷
     */
    void insertCouponByPoints(UserCouponVO userCouponDO);

    PageResult<UserCouponPageRespVo> userCouponList(UserCouponPageReqVo couponPageReqVo);

    void exportUserCouponList(UserCouponPageReqVo couponPageReqVo);

    UserCouponCountRespVo getCount(Long couponId);

    void memberDayCoupon();

    void memberCardBenefitJob();

    /**
     * 优惠券去使用
     *
     * @param couponId couponId
     * @param storeId  storeId
     * @return ItemDto
     */
    ItemDto couponIsUsed(Long couponId, Long storeId);


    /**
     * 领取抖音优惠券
     *
     * @param claimCoupon claimCoupon
     * @return Boolean
     */
    Long claimTiktokCoupon(ClaimTiktokCouponReqVO claimCoupon);

    /**
     * 批量发放优惠券（会员管理页面）
     * @param result memberIds
     * @param couponId couponId
     * @param sendNum sendNum
     * @return Boolean
     */
    Boolean sendCoupon(Set<MemberCouponDTO> result, Long couponId, Integer sendNum);

    /**
     * 批量发放优惠券包（会员管理页面）
     * @param result memberIds
     * @param couponId couponId
     * @param sendNum sendNum
     * @return Boolean
     */
    Boolean sendPackage(Set<MemberCouponDTO> result, Long couponId, Integer sendNum);

    /**
     * 领取秒杀优惠券
     * @param claimCoupon claimCoupon
     * @return Boolean
     */
    Boolean claimSeckillCoupon(ClaimCouponReqVO claimCoupon);

    /**
     * 领取集点优惠券
     * @param claimCoupon claimCoupon
     * @return Boolean
     */
    Boolean claimPointsCoupon(ClaimPointsCouponReqVO claimCoupon);

    Boolean insertUserCouponWithSeckill(Long userId, GoodCouponRespVO goodCoupon, WxMemberDTO wxMember, Integer couponSource, Long storeId);

    Boolean insertUserCouponWithSeckillBatch(Long userId, List<GoodCouponRespVO> goodCoupons, WxMemberDTO wxMember, Integer couponSource, Long storeId);

    Integer insertMemberCardBenefitCouponsBatch(List<UserCouponDO> userCoupons);

    Integer insertMemberCardBenefitCouponsShardChunk(Integer shardingValue, List<UserCouponDO> userCoupons);

    Boolean insertCouponPackageWithPoints(CouponPackageRespVO couponPackageRespVO, WxMemberDTO wxMember, Integer userRestrictions, Long storeId, Integer couponSource);

    /**
     * 批量发放优惠券（会员日发放）
     * @return Boolean
     */
    Boolean sendCoupons(Set<MemberCouponDTO> resul);

    /**
     * 注册发放优惠券
     * @param memberCouponDTO memberCouponDTO
     */
    void insertCouponWithRegister(MemberCouponDTO memberCouponDTO);
}
