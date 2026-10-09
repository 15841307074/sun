package com.htyoudao.youdao.module.promotion.service.usercouponrecord;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.CouponAmountVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateRespV2VO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponRecordDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券使用记录 Service 接口
 * @author 13149747939
 */
public interface UserCouponRecordService {

    /**
     * 根据优惠券id查询优惠券使用记录
     * @param ids ids
     * @return UserCouponRecordDO
     */
    List<UserCouponRecordDO> listByCouponIds(List<Long> ids);

    /**
     * 获取昨天之前的数据
     * @param yesterdayLastSecond yesterdayLastSecond
     * @param id id
     * @return UserCouponRecordDO
     */
    List<UserCouponRecordDO> getYesterDayDataList(LocalDateTime yesterdayLastSecond, Long id);

    /**
     * 根据优惠券id分页查询优惠券使用记录
     * @param page page
     * @param goodCouponDateReqVO goodCouponDateReqVO
     * @return UserCouponRecordDO
     */
    Page<GoodCouponDateRespVO> listPageByCouponId(Page<GoodCouponDateRespVO> page, GoodCouponDateReqVO goodCouponDateReqVO);

    /**
     * 查询优惠券使用金额
     * @param ids ids
     * @return List<CouponAmountVO>
     */
    List<CouponAmountVO> selectCouponAmount(List<Long> ids);
}