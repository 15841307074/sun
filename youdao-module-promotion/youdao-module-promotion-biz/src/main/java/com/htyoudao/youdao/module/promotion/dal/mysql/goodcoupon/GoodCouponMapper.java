package com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.PointsProductCouponDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 优惠券 Mapper
 *
 * @author dht
 */
@Mapper
public interface GoodCouponMapper extends BaseMapperX<GoodCouponDO> {

    /**
     * 查询积分商品详情专用的完整优惠券数据。
     *
     * @param couponCode 优惠券编码
     * @return 最新一条有效优惠券
     */
    @Select("""
            SELECT *
            FROM good_coupon
            WHERE coupon_code = #{couponCode}
              AND deleted = 0
            ORDER BY id DESC
            LIMIT 1
            """)
    PointsProductCouponDetailVO selectPointsProductCouponByCode(
            @Param("couponCode") String couponCode);

    /**
     * 分页查询优惠券
     * @param reqVO reqVO
     * @return GoodCouponDO
     */
    default PageResult<GoodCouponDO> selectPage(GoodCouponPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GoodCouponDO>()
                .eqIfPresent(GoodCouponDO::getCouponCode, reqVO.getCouponCode())
                .likeIfPresent(GoodCouponDO::getCouponName, reqVO.getCouponName())
                .eqIfPresent(GoodCouponDO::getCouponType, reqVO.getCouponType())
                .eqIfPresent(GoodCouponDO::getCouponNum, reqVO.getCouponNum())
                .eqIfPresent(GoodCouponDO::getLimitNum, reqVO.getLimitNum())
                .eqIfPresent(GoodCouponDO::getCommunityFlag, reqVO.getCommunityFlag())
                .betweenIfPresent(GoodCouponDO::getCouponStartTime, reqVO.getCouponStartTime())
                .betweenIfPresent(GoodCouponDO::getCouponEndTime, reqVO.getCouponEndTime())
                .eqIfPresent(GoodCouponDO::getReceivedNum, reqVO.getReceivedNum())
                .eqIfPresent(GoodCouponDO::getUsedNum, reqVO.getUsedNum())
                .eqIfPresent(GoodCouponDO::getSingleIds, reqVO.getSingleIds())
                .eqIfPresent(GoodCouponDO::getFullReduction, reqVO.getFullReduction())
                .eqIfPresent(GoodCouponDO::getReduceAmount, reqVO.getReduceAmount())
                .eqIfPresent(GoodCouponDO::getDiscount, reqVO.getDiscount())
                .eqIfPresent(GoodCouponDO::getIsGround, reqVO.getIsGround())
                .eqIfPresent(GoodCouponDO::getDistributionMethod, reqVO.getDistributionMethod())
                .eqIfPresent(GoodCouponDO::getCouponExplain, reqVO.getCouponExplain())
                .eqIfPresent(GoodCouponDO::getUseRules, reqVO.getUseRules())
                .eqIfPresent(GoodCouponDO::getCouponImageUrl, reqVO.getCouponImageUrl())
                .betweenIfPresent(GoodCouponDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(GoodCouponDO::getUseType, reqVO.getUseType())
                .betweenIfPresent(GoodCouponDO::getUseTime, reqVO.getUseTime())
                .eqIfPresent(GoodCouponDO::getRemark, reqVO.getRemark())
                .eqIfPresent(GoodCouponDO::getIsCommon, reqVO.getIsCommon())
                .eqIfPresent(GoodCouponDO::getCommunityQrImage, reqVO.getDeptIds())
                .eqIfPresent(GoodCouponDO::getUserRestrictions, reqVO.getUserRestrictions())
                .eqIfPresent(GoodCouponDO::getIsShare, reqVO.getIsShare())
                .eqIfPresent(GoodCouponDO::getDoorsillType, reqVO.getDoorsillType())
                .eqIfPresent(GoodCouponDO::getDoorsill, reqVO.getDoorsill())
                .eqIfPresent(GoodCouponDO::getIsCommonStore, reqVO.getIsCommonStore())
                .eqIfPresent(GoodCouponDO::getReliefOrDiscount, reqVO.getReliefOrDiscount())
                .eqIfPresent(GoodCouponDO::getPayAmount, reqVO.getPayAmount())
                .betweenIfPresent(GoodCouponDO::getShowTime, reqVO.getShowTime())
                .eqIfPresent(GoodCouponDO::getVersion, reqVO.getVersion())
                .eqIfPresent(GoodCouponDO::getTotalNum, reqVO.getTotalNum())
                .eqIfPresent(GoodCouponDO::getMemberLevel, reqVO.getMemberLevel())
                .eqIfPresent(GoodCouponDO::getDayLimit, reqVO.getDayLimit())
                .eqIfPresent(GoodCouponDO::getCouponNumVisible, reqVO.getCouponNumVisible())
                .eqIfPresent(GoodCouponDO::getHabit, reqVO.getHabit())
                .eqIfPresent(GoodCouponDO::getDayNumbers, reqVO.getDayNumbers())
                .eqIfPresent(GoodCouponDO::getWeekNumbers, reqVO.getWeekNumbers())
                .eqIfPresent(GoodCouponDO::getTimeRange, reqVO.getTimeRange())
                .eqIfPresent(GoodCouponDO::getIsAllDay, reqVO.getIsAllDay())
                .eqIfPresent(GoodCouponDO::getCouponBgImageUrl, reqVO.getCouponBgImageUrl())
                .eqIfPresent(GoodCouponDO::getMiniSortUrl, reqVO.getMiniSortUrl())
                .eqIfPresent(GoodCouponDO::getH5SortUrl, reqVO.getH5SortUrl())
                .orderByDesc(GoodCouponDO::getCreateTime)
                .inIfPresent(GoodCouponDO::getCouponType, reqVO.getCouponTypes()));
    }

    /**
     * 更新优惠券领取数量
     * @param couponId couponId
     * @param storeId storeId
     * @param claimNum claimNum
     */
    void upsertCouponStoreClaim(@Param("couponId") long couponId,
                                @Param("storeId") long storeId,
                                @Param("claimNum") int claimNum);
}
