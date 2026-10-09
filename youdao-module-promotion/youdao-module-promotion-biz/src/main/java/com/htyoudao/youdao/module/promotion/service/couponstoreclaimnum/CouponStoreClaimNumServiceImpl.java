package com.htyoudao.youdao.module.promotion.service.couponstoreclaimnum;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstoreclaimnum.CouponStoreClaimNumDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponstoreclaimnum.CouponStoreClaimNumMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dht
 */
@Service
public class CouponStoreClaimNumServiceImpl implements CouponStoreClaimNumService{

    @Resource
    private CouponStoreClaimNumMapper couponStoreClaimNumMapper;

    @Override
    public List<CouponStoreClaimNumDO> getByCouponId(Long id) {
        LambdaQueryWrapper<CouponStoreClaimNumDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponStoreClaimNumDO::getCouponId, id);
        return couponStoreClaimNumMapper.selectList(queryWrapper);
    }
}
