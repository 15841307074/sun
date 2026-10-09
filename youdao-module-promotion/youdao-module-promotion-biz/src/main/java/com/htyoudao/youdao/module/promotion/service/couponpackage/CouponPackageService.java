package com.htyoudao.youdao.module.promotion.service.couponpackage;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.couponpackage.vo.ClaimCouponPackageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 优惠券包 Service 接口
 *
 * @author 13149747939
 */
public interface CouponPackageService {


    /**
     * 创建优惠券包
     * @param createReqVO createReqVO
     * @return Boolean
     */
    Boolean insert(CouponPackageSaveReqVO createReqVO);

    /**
     * 修改优惠券包
     * @param updateReqVO updateReqVO
     * @return Boolean
     */
    Boolean update(CouponPackageSaveReqVO updateReqVO);

    /**
     * 领取优惠券包
     * @param claimCouponPackageReqVO claimCouponPackageReqVO
     * @return Boolean
     */
    Boolean claimCouponPackage(ClaimCouponPackageReqVO claimCouponPackageReqVO);

    /**
     * 领取优惠券包 通用版
     * source 来源请参考 {@link CouponSourceType}
     * @return Boolean
     */
    Boolean claimCouponPackage(WxMemberDTO wxMember, Long packageId, Integer source,Integer marketId, Long storeId);

    /**
     * 领取周周优惠券包
     * @param claimCouponPackageReqVO claimCouponPackageReqVO
     * @return Boolean
     */
    Boolean claimZZCouponPackage(ClaimCouponPackageReqVO claimCouponPackageReqVO);

    /**
     * 领取优惠券包
     * @param claimCouponPackageReqVO claimCouponPackageReqVO
     * @return Boolean
     */
    Boolean claimSmsCouponPackage(ClaimCouponPackageReqVO claimCouponPackageReqVO);


    PageResult<CouponPackageRespVO> getPageCouponPackage(@Valid GetCouponPackagePageReqVO pageReqVO);

    CouponPackageRespVO selectById(Long id);

    void removeById(Long id);

    void editPackageNum(CouponPackageUpdateNumVO couponPackageAO);

    void updateIsGround(CouponPackageUpdateGroundVO couponPackageUpdateGroundVO);


    void updateById(CouponPackageDO couponPackage);

    void updateIsGroundById(Long packageId, int isGround0);

    void updateReceivedNumAndPackageNumById(Integer num, Long packageId);

    PageResult<CouponPackageCollectRespVO> couponPackageList(CouponPackageCollectReqVO collectReqVO);

    CouponPackageDO couponPackageInfo(Long id);

    CouponPackageCountRespVo getCouponPackageCount(Long id);

    /**
     * 定时同步优惠券门店领取数量
     */
    void nocCouponStoreNum();

    /**
     * 根据优惠券包id查询优惠券门店领取数量
     * @param packageIds packageIds
     * @return long
     */
    long selectCountByIds(List<Long> packageIds);

    /**
     * 批量发放优惠券包（会员管理页面）
     * @param result memberIds
     * @param couponId couponId
     * @param sendNum sendNum
     * @return Boolean
     */
    Boolean sendCoupon(Set<MemberCouponDTO> result, Long couponId, Integer sendNum);

    /**
     * 更新所有h5
     * @return
     */
    Integer updateAllH5();

    /**
     * 获取社区二维码
     * @param couponId couponId
     * @return List<String>
     */
    List<String> getCommunityQrImage(Long couponId);

    /**
     * 根据券包 id 批量查询券包名称
     *
     * @param packageIds packageIds
     * @return Map<Long, String>
     */
    Map<Long, String> getPackageNameMap(List<Long> packageIds);
}
