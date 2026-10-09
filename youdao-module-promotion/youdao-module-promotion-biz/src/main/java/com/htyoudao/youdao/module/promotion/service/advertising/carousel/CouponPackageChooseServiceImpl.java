package com.htyoudao.youdao.module.promotion.service.advertising.carousel;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponPackageChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponPackageChoosedReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponPackageChooseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertising.carousel.CouponPackageChooseMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertisingImage.AdvertisingImageMapper;
import com.htyoudao.youdao.module.promotion.util.page.PageUtils;
import com.htyoudao.youdao.module.promotion.util.string.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


@Service
public class CouponPackageChooseServiceImpl implements CouponPackageChooseService{

    @Resource
    private CouponPackageChooseMapper couponPackageChooseMapper;

    @Resource
    private AdvertisingImageMapper advertisingImageMapper;

    @Override
    public PageResult<CouponPackageChooseDO> getPage(CouponPackageChooseReqVO couponPackageChooseReqVO) {

        LambdaQueryWrapper<CouponPackageChooseDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CouponPackageChooseDO::getPackageType,
                couponPackageChooseReqVO.getPackageType()==null?0:couponPackageChooseReqVO.getPackageType());
        if(StringUtils.isNotBlank(couponPackageChooseReqVO.getCouponPackageNameOrRemark())){
            lambdaQueryWrapper
                    .and(wrapper -> wrapper
                            .like(CouponPackageChooseDO::getPackageName, couponPackageChooseReqVO.getCouponPackageNameOrRemark())
                            .or()
                            .like(CouponPackageChooseDO::getRemark, couponPackageChooseReqVO.getCouponPackageNameOrRemark())
                    );
        }
        if(ObjectUtil.isNotEmpty(couponPackageChooseReqVO.getUserRestrictions())){
            lambdaQueryWrapper.eq(CouponPackageChooseDO::getUserRestrictions, couponPackageChooseReqVO.getUserRestrictions());
        }

        lambdaQueryWrapper.eq(CouponPackageChooseDO::getIsGround, 1);

        // 创建时间倒排
        lambdaQueryWrapper.orderBy(true,false, CouponPackageChooseDO::getCreateTime,CouponPackageChooseDO::getId);

        PageParam page = new PageParam();
        page.setPageNo(couponPackageChooseReqVO.getPageNo());
        page.setPageSize(couponPackageChooseReqVO.getPageSize());

        return couponPackageChooseMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public CouponPackageChooseDO getChoose(CouponPackageChoosedReqVO couponPackageChoosedReqVO) {
        AdvertisingImageDO advertisingImageDO = advertisingImageMapper.selectById(couponPackageChoosedReqVO.getAdvertisingImageId());
        // 获取已选中的优惠券ID
        return couponPackageChooseMapper.selectById(advertisingImageDO.getCouponBagId());
    }
}
