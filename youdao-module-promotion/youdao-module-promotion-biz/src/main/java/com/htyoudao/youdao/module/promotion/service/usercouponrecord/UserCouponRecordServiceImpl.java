package com.htyoudao.youdao.module.promotion.service.usercouponrecord;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.CouponAmountVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateRespV2VO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord.UserCouponRecordMapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券使用记录 Service 实现类
 *
 * @author dht
 */
@Service
@Validated
public class UserCouponRecordServiceImpl implements UserCouponRecordService {

    @Resource
    private UserCouponRecordMapper userCouponRecordMapper;

    @Override
    public List<UserCouponRecordDO> listByCouponIds(List<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return List.of();
        }
        QueryWrapper<UserCouponRecordDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("coupon_id", ids);
        return userCouponRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<UserCouponRecordDO> getYesterDayDataList(LocalDateTime yesterdayLastSecond, Long id) {
        LambdaQueryWrapper<UserCouponRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(UserCouponRecordDO::getCreateTime, yesterdayLastSecond);
        queryWrapper.eq(UserCouponRecordDO::getCouponId, id);
        return userCouponRecordMapper.selectList(queryWrapper);
    }

    @Override
    public Page<GoodCouponDateRespVO> listPageByCouponId(Page<GoodCouponDateRespVO> page, GoodCouponDateReqVO goodCouponDateReqVO) {
        return userCouponRecordMapper.listPageByCouponId(page,goodCouponDateReqVO);
    }

    @Override
    public List<CouponAmountVO> selectCouponAmount(List<Long> ids) {
        return userCouponRecordMapper.selectCouponAmount(ids);
    }
}