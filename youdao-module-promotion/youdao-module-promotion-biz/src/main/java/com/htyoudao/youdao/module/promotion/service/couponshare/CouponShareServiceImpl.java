package com.htyoudao.youdao.module.promotion.service.couponshare;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.CouponShareRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShareSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponshare.CouponShareDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponshare.CouponShareMapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

/**
 * 优惠券分享子 Service 实现类
 *
 * @author 13149747939
 */
@Service
@Validated
public class CouponShareServiceImpl implements CouponShareService {

    @Resource
    private CouponShareMapper couponShareMapper;

    @Override
    public Boolean saveOrUpdate(ShareSaveReqVO shareSaveReqVO) {
        CouponShareDO couponShareDO = BeanUtils.toBean(shareSaveReqVO, CouponShareDO.class);
        return couponShareMapper.insertOrUpdate(couponShareDO);
    }

    @Override
    public CouponShareDO getById(Long id) {
        return couponShareMapper.selectById(id);
    }

    @Override
    public CouponShareRespVO getByCouponId(Long id) {
        QueryWrapper<CouponShareDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CouponShareDO::getCouponId, id);
        CouponShareDO couponShareDO = couponShareMapper.selectOne(queryWrapper);
        return BeanUtils.toBean(couponShareDO, CouponShareRespVO.class);
    }

    @Override
    public void insert(ShareSaveReqVO shareSaveReqVO) {
        CouponShareDO couponShareDO = BeanUtils.toBean(shareSaveReqVO, CouponShareDO.class);
        couponShareMapper.insert(couponShareDO);
    }

    @Override
    public void updateTitle(ShareSaveReqVO shareSaveReqVO) {
        LambdaUpdateWrapper<CouponShareDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(CouponShareDO::getCouponId,shareSaveReqVO.getCouponId());
        lambdaUpdateWrapper.set(CouponShareDO::getShareTitle,shareSaveReqVO.getShareTitle());
        couponShareMapper.update(lambdaUpdateWrapper);
    }
}