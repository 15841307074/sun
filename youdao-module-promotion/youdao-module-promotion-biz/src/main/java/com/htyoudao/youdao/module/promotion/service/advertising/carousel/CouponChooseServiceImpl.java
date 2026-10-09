package com.htyoudao.youdao.module.promotion.service.advertising.carousel;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChoosedReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponPageRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponChooseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertising.carousel.CouponChooseMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertisingImage.AdvertisingImageMapper;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.CouponTypeEnum;
import com.htyoudao.youdao.module.promotion.service.couponcommodity.CouponCommodityService;
import com.htyoudao.youdao.module.promotion.util.page.PageUtils;
import com.htyoudao.youdao.module.promotion.util.string.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
public class CouponChooseServiceImpl implements CouponChooseService{

    @Resource
    private CouponChooseMapper couponChooseMapper;

    @Resource
    private CouponCommodityService couponCommodityService;

    @Resource
    private AdvertisingImageMapper advertisingImageMapper;
    @Override
    public PageResult<CouponChooseDO> getPage(CouponChooseReqVO couponChooseReqVO) {
        LambdaQueryWrapper<CouponChooseDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(couponChooseReqVO.getCouponNameOrRemark())){
            lambdaQueryWrapper
                    .and(wrapper -> wrapper
                            .like(CouponChooseDO::getCouponName, couponChooseReqVO.getCouponNameOrRemark())
                            .or()
                            .like(CouponChooseDO::getRemark, couponChooseReqVO.getCouponNameOrRemark())
                    );
        }
        lambdaQueryWrapper.in(CouponChooseDO::getCouponType, CouponTypeEnum.VALUES_LIST);
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getIsCommon())){
            lambdaQueryWrapper.eq(CouponChooseDO::getIsCommon, couponChooseReqVO.getIsCommon());
        }

        List<Long> goodCouponIds = new ArrayList<>();
        //查询优惠券商品表
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getCommodityId())){
            List<CouponCommodityDO> couponCommodityDOList = couponCommodityService.selectByCommodityId(Long.valueOf(couponChooseReqVO.getCommodityId()));
            if (ObjectUtil.isNotEmpty(couponCommodityDOList)){
                goodCouponIds.addAll(couponCommodityDOList.stream().map(CouponCommodityDO::getCouponId).toList());
            }
        }
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getCouponType())){
            lambdaQueryWrapper.eq(CouponChooseDO::getCouponType, couponChooseReqVO.getCouponType());
        }
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getUserRestrictions())){
            lambdaQueryWrapper.eq(CouponChooseDO::getUserRestrictions, couponChooseReqVO.getUserRestrictions());
        }
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getHabit())){
            lambdaQueryWrapper.eq(CouponChooseDO::getHabit, couponChooseReqVO.getHabit());
        }
        // 上架状态
        lambdaQueryWrapper.eq(CouponChooseDO::getIsGround,1);

        // 有效期内
        lambdaQueryWrapper.apply("((use_type = 0 AND coupon_end_time > CURRENT_TIMESTAMP) OR use_type IN (1,2,3))");


        if(ObjectUtil.isNotEmpty(goodCouponIds)){
            lambdaQueryWrapper.in(true, CouponChooseDO::getId, goodCouponIds);
        }

        // 创建时间倒排
        lambdaQueryWrapper.orderBy(true,false,CouponChooseDO::getCreateTime,CouponChooseDO::getId);
        PageParam page = new PageParam();
        page.setPageNo(couponChooseReqVO.getPageNo());
        page.setPageSize(couponChooseReqVO.getPageSize());


        return couponChooseMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public PageResult<CouponChooseDO> asyncPage(CouponChooseReqVO couponChooseReqVO) {
        LambdaQueryWrapper<CouponChooseDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(couponChooseReqVO.getCouponNameOrRemark())){
            lambdaQueryWrapper
                    .and(wrapper -> wrapper
                            .like(CouponChooseDO::getCouponName, couponChooseReqVO.getCouponNameOrRemark())
                            .or()
                            .like(CouponChooseDO::getRemark, couponChooseReqVO.getCouponNameOrRemark())
                    );
        }

        // 类型
        lambdaQueryWrapper.in(CouponChooseDO::getCouponType, CouponTypeEnum.VALUES_LIST);
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getIsCommon())){
            lambdaQueryWrapper.eq(CouponChooseDO::getIsCommon, couponChooseReqVO.getIsCommon());
        }

        List<Long> goodCouponIds = new ArrayList<>();
        //查询优惠券商品表
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getCommodityId())){
            List<CouponCommodityDO> couponCommodityDOList = couponCommodityService.selectByCommodityId(Long.valueOf(couponChooseReqVO.getCommodityId()));
            if (ObjectUtil.isNotEmpty(couponCommodityDOList)){
                goodCouponIds.addAll(couponCommodityDOList.stream().map(CouponCommodityDO::getCouponId).toList());
            }
        }
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getCouponType())){
            lambdaQueryWrapper.eq(CouponChooseDO::getCouponType, couponChooseReqVO.getCouponType());
        }
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getUserRestrictions())){
            lambdaQueryWrapper.eq(CouponChooseDO::getUserRestrictions, couponChooseReqVO.getUserRestrictions());
        }
        if(ObjectUtil.isNotEmpty(couponChooseReqVO.getHabit())){
            lambdaQueryWrapper.eq(CouponChooseDO::getHabit, couponChooseReqVO.getHabit());
        }
        // 无需上架状态
        // lambdaQueryWrapper.eq(CouponChooseDO::getIsGround,1);




        if(ObjectUtil.isNotEmpty(goodCouponIds)){
            lambdaQueryWrapper.in(true, CouponChooseDO::getId, goodCouponIds);
        }

        // 创建时间倒排
        lambdaQueryWrapper.orderBy(true,false,CouponChooseDO::getCreateTime,CouponChooseDO::getId);
        PageParam page = new PageParam();
        page.setPageNo(couponChooseReqVO.getPageNo());
        page.setPageSize(couponChooseReqVO.getPageSize());


        return couponChooseMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public CouponChooseDO getChoose(CouponChoosedReqVO couponChoosedReqVO) {
        AdvertisingImageDO advertisingImageDO = advertisingImageMapper.selectById(couponChoosedReqVO.getAdvertisingImageId());
        // 获取已选中的优惠券ID
        return couponChooseMapper.selectById(advertisingImageDO.getCouponId());
    }


}
