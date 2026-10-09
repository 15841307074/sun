package com.htyoudao.youdao.module.system.dal.mysql.goodcoupon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;

import com.htyoudao.youdao.module.system.dal.dataobject.goodcoupon.GoodCouponDO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectKey;

import java.util.List;


/**
 * 优惠券Mapper接口
 *
 */
@Mapper
public interface GoodCouponMapper extends BaseMapper<GoodCouponDO>
{


}
