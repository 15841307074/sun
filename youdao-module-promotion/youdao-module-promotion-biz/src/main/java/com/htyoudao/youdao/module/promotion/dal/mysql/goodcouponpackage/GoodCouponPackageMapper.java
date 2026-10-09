package com.htyoudao.youdao.module.promotion.dal.mysql.goodcouponpackage;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.GoodCouponPackagePageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 优惠券包关系 Mapper
 *
 * @author 13149747939
 */
@Mapper
public interface GoodCouponPackageMapper extends BaseMapperX<GoodCouponPackageDO> {

    /**
     * 获取优惠券包关系分页
     * @param reqVO reqVO
     * @return PageResult<CouponPackageDO>
     */
    default PageResult<GoodCouponPackageDO> selectPage(GoodCouponPackagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GoodCouponPackageDO>()
                .eqIfPresent(GoodCouponPackageDO::getPackageId, reqVO.getPackageId())
                .eqIfPresent(GoodCouponPackageDO::getCouponId, reqVO.getCouponId())
                .eqIfPresent(GoodCouponPackageDO::getNum, reqVO.getNum())
                .orderByDesc(GoodCouponPackageDO::getId));
    }

  //  List<GoodCouponPackageDO> selectListByPackageId(Long packageId);


}