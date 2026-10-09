package com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.CouponDataDownloadReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.CouponAmountVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateRespV2VO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreAndOrgDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 优惠券使用记录 Mapper
 *
 * @author dht
 */
@Mapper
public interface UserCouponRecordMapper extends BaseMapperX<UserCouponRecordDO> {


    /**
     * 分页查询优惠券使用记录
     * @param page page
     * @param goodCouponDateReqVO goodCouponDateReqVO
     * @return UserCouponRecordDO
     */
    Page<GoodCouponDateRespVO> listPageByCouponId(@Param("page") Page<GoodCouponDateRespVO> page, @Param("goodCouponDateReqVO") GoodCouponDateReqVO goodCouponDateReqVO);

    /**
     * 查询优惠券使用记录
     * @param couponIds couponIds
     * @return List<CouponAmountVO>
     */
    List<CouponAmountVO> selectCouponAmount(@Param("couponIds") List<Long> couponIds);

    List<CouponStoreAndOrgDO> getStoresByCouponIdAndName(Long couponId, String storeName, Long businessId);

    /**
     * 获取优惠券使用记录
     * @param couponDataDownloadReqVO couponDataDownloadReqVO
     */
    List<GoodCouponDateRespVO> getDataByDay(@Param("couponDataDownloadReqVO") CouponDataDownloadReqVO couponDataDownloadReqVO);

    List<GoodCouponDateRespVO> getDataByMonth(@Param("couponDataDownloadReqVO") CouponDataDownloadReqVO couponDataDownloadReqVO);

    List<GoodCouponDateRespVO> getDataBySummary(@Param("couponDataDownloadReqVO") CouponDataDownloadReqVO couponDataDownloadReqVO);

    List<GoodCouponDateRespVO> getDataByDaySource(@Param("couponDataDownloadReqVO") CouponDataDownloadReqVO couponDataDownloadReqVO);

    List<GoodCouponDateRespVO> getDataByMonthSource(@Param("couponDataDownloadReqVO") CouponDataDownloadReqVO couponDataDownloadReqVO);

    List<GoodCouponDateRespVO> getDataBySourceSum(@Param("couponDataDownloadReqVO") CouponDataDownloadReqVO couponDataDownloadReqVO);

    /**
     * 按门店批量聚合查询用券记录（消除N+1查询）
     * @param couponId 优惠券ID
     * @param storeIds 门店ID列表
     * @return 每个门店的聚合数据
     */
    List<Map<String, Object>> selectAggregateByStoresAndCoupon(@Param("couponId") Long couponId,
                                                               @Param("storeIds") List<Long> storeIds);
}