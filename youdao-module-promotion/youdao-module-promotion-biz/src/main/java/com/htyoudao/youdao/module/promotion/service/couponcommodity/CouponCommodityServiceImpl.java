package com.htyoudao.youdao.module.promotion.service.couponcommodity;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCommodityVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponcommodity.CouponCommodityMapper;
import com.htyoudao.youdao.module.promotion.util.ConvertUtil;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * 优惠券门店关系 Service 实现类
 *
 * @author 13149747939
 */
@Service
@Validated
public class CouponCommodityServiceImpl implements CouponCommodityService {

    @Resource
    private CouponCommodityMapper commodityMapper;

    @DubboReference
    private CommodityApi commodityApi;


    @Override
    public void insertBatch(List<CouponCommodityDO> couponCommodities) {
        commodityMapper.insertBatchSomeColumn(couponCommodities);
    }

    @Override
    public void deleteByCouponId(Long couponId) {
        QueryWrapper<CouponCommodityDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id", couponId);
        commodityMapper.delete(queryWrapper);
    }

    @Override
    public List<CouponCommodityDO> selectByCouponId(Long id) {
        QueryWrapper<CouponCommodityDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id", id);
        return commodityMapper.selectList(queryWrapper);
    }

    @Override
    public List<CouponCommodityDO> selectByCommodityId(Long commodityId) {
        LambdaQueryWrapper<CouponCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponCommodityDO::getCommodityId, commodityId);
        return commodityMapper.selectList(queryWrapper);
    }

    @Override
    public List<CouponCommodityDO> selectByCouponIds(List<Long> respGoodCouponIds) {
        LambdaQueryWrapper<CouponCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CouponCommodityDO::getCouponId, respGoodCouponIds);
        return commodityMapper.selectList(queryWrapper);
    }

    @Override
    public List<AppCouponCommodityVO> selectCouponCommodity(Long couponId) {
        LambdaQueryWrapper<CouponCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponCommodityDO::getCouponId, couponId);
        List<CouponCommodityDO> couponCommodityDOS = commodityMapper.selectList(queryWrapper);
        List<AppCouponCommodityVO> appCouponCommodityVOS = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(couponCommodityDOS)) {
            List<Long> commodityIds = couponCommodityDOS.stream().map(CouponCommodityDO::getCommodityId).toList();
            CommonResult<List<CommodityDTO>> commodityList = commodityApi.getCommodityList(commodityIds);
            List<CommodityDTO> data = commodityList.getData();
            for (CommodityDTO datum : data) {
                AppCouponCommodityVO appCouponCommodityVO = new AppCouponCommodityVO();
                appCouponCommodityVO.setName(datum.getCommodityName());
                if (ObjectUtil.isNotEmpty(datum.getImageUrl())) {
                    appCouponCommodityVO.setImageUrl(ConvertUtil.convertStringToListS(datum.getImageUrl()).get(0));
                }
                appCouponCommodityVOS.add(appCouponCommodityVO);
            }

        }

        return appCouponCommodityVOS;
    }


    @Override
    public Boolean selectCouponHaveCommodity(Long commodityId) {
        LambdaQueryWrapper<CouponCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponCommodityDO::getCommodityId, commodityId);
        List<CouponCommodityDO> couponCommodityDOS = commodityMapper.selectList(queryWrapper);
        if (ObjectUtil.isNotEmpty(couponCommodityDOS)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean updateCommodityName(Long commodityId, String commodityName) {
        LambdaUpdateWrapper<CouponCommodityDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CouponCommodityDO::getCommodityId, commodityId);
        updateWrapper.set(CouponCommodityDO::getCommodityName, commodityName);
       commodityMapper.update(updateWrapper);
       return Boolean.TRUE;
    }
}