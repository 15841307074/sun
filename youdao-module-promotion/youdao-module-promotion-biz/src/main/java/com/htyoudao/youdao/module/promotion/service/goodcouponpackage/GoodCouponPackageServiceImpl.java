package com.htyoudao.youdao.module.promotion.service.goodcouponpackage;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.GoodCouponPackagePageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.GoodCouponPackageSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcouponpackage.GoodCouponPackageMapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;

import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_PACKAGE_NOT_EXISTS;

/**
 * 优惠券包关系 Service 实现类
 *
 * @author dht
 */
@Service
@DS(DsNameConstants.SHARDING)
public class GoodCouponPackageServiceImpl implements GoodCouponPackageService {

    @Resource
    private GoodCouponPackageMapper goodCouponPackageMapper;

    @Override
    public List<GoodCouponPackageDO> selectListByPackageId(Long id) {
        LambdaQueryWrapper<GoodCouponPackageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodCouponPackageDO::getPackageId, id);
        return goodCouponPackageMapper.selectList(queryWrapper);
    }

    @Override
    public Boolean insertBatch(List<GoodCouponPackageDO> goodCouponPackages) {
        return goodCouponPackageMapper.insertBatch(goodCouponPackages);
    }

    @Override
    public List<GoodCouponPackageDO> selectListByCouponId(Long couponId) {
        LambdaQueryWrapper<GoodCouponPackageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodCouponPackageDO::getCouponId, couponId);
        return goodCouponPackageMapper.selectList(queryWrapper);
    }
}