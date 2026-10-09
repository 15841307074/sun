package com.htyoudao.youdao.module.member.api.pointsproduct;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.pointsproduct.dto.PointsProductDTO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;
import com.htyoudao.youdao.module.member.service.pointsProduct.IPointsProductService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author dht
 */
@DubboService
public class PointsProductApiImpl implements PointsProductApi{

    @Resource
    private IPointsProductService pointsProductService;

    @Override
    public CommonResult<PointsProductDTO> getById(Long productId) {
        com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO  ww = new com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO();
        LambdaQueryWrapper<com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO::getProductId,productId);
        PointsProductDO byId = pointsProductService.getOne(wrapper);

        return CommonResult.success(BeanUtil.toBean(byId, PointsProductDTO.class));
    }

    @Override
    public void updateRest(Long productId) {
        LambdaQueryWrapper<PointsProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsProductDO::getProductId,productId);
        PointsProductDO one = pointsProductService.getOne(wrapper);
        if(one!=null){
            LambdaUpdateWrapper<PointsProductDO> productUpdateWrapper = new LambdaUpdateWrapper<>();
            productUpdateWrapper.set(PointsProductDO::getProductInventory, one.getProductInventory()-1);
            productUpdateWrapper.eq(PointsProductDO::getProductId, productId);
            pointsProductService.update(productUpdateWrapper);
        }

    }

    @Override
    public CommonResult<Long> getCountByCouponCode(String couponCode) {
        return pointsProductService.getCountByCouponCode(couponCode);
    }
}
