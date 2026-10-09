package com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponDataAnalysisBySourceDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券使用记录 Mapper
 *
 * @author dht
 */
@Mapper
public interface UserCouponRecordDataAnanlysisBySourceMapper extends BaseMapperX<UserCouponDataAnalysisBySourceDO> {
}