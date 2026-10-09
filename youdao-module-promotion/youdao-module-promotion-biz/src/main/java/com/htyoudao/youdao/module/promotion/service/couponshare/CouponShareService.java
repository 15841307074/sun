package com.htyoudao.youdao.module.promotion.service.couponshare;


import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.CouponShareRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShareSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponshare.CouponShareDO;

/**
 * 优惠券分享子 Service 接口
 *
 * @author 13149747939
 */
public interface CouponShareService {

    /**
     * 新建/修改优惠券分享
     * @param shareSaveReqVO shareSaveReqVO
     * @return Integer
     */
    Boolean saveOrUpdate(ShareSaveReqVO shareSaveReqVO);

    /**
     * 根据id查询优惠券分享
     * @param id id
     * @return CouponShareDO
     */
    CouponShareDO getById(Long id);

    /**
     * 根据优惠券id查询优惠券分享
     * @param id id
     * @return CouponShareRespVO
     */
    CouponShareRespVO getByCouponId(Long id);

    /**
     * 新建优惠券分享
     * @param shareSaveReqVO shareSaveReqVO
     */
    void insert(ShareSaveReqVO shareSaveReqVO);

    /**
     * 修改优惠券分享标题
     * @param shareSaveReqVO shareSaveReqVO
     */
    void updateTitle(ShareSaveReqVO shareSaveReqVO);
}