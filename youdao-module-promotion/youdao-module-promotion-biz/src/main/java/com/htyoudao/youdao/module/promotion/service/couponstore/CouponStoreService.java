package com.htyoudao.youdao.module.promotion.service.couponstore;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.enums.coupon.CouponStoreTagSqlTypeEnum;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.UpdateCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreAndOrgDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;

import java.util.List;

/**
 * 优惠券门店关系 Service 接口
 *
 * @author 13149747939
 */
public interface CouponStoreService {

    /**
     * 批量插入优惠券门店关系
     * @param couponStores couponStores
     */
    void insertBatch(List<CouponStoreDO> couponStores);

    /**
     * 根据优惠券id删除优惠券门店关系
     * @param couponId couponId
     */
    void deleteByCouponId(Long couponId);

    /**
     * 根据优惠券id查询优惠券门店关系
     * @param id id
     * @return CouponStoreDO
     */
    List<CouponStoreDO> selectByCouponId(Long id);

    /**
     * 根据优惠券id查询优惠券门店关系数量
     * @param couponId couponId
     * @return long
     */
    long selectCountByCouponId(Long couponId);

    List<CouponStoreDO> selectByStoreIds(List<Long> storeIds);

    List<CouponStoreDO> selectByCouponIds(List<Long> respGoodCouponIds);

    List<CouponStoreAndOrgDO> getStoresByCouponIdAndName(Long couponId, String storeName);

    /**
     * 修改优惠券和券包绑定的门店
     * @param goodCouponDTO goodCouponDTO
     */
    void updateCouponStore(GoodCouponStoreDTO goodCouponDTO);

    /**
     * 通过标签id和门店id修改优惠券门店关系
     * @param couponStores couponStores
     * @param sqlType sqlType
     */
    void updateCouponStoreByTagIdAndStoreId(List<UpdateCouponStoreDTO> couponStores, CouponStoreTagSqlTypeEnum sqlType);
}