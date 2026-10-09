package com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class CouponStoreAndOrgDO extends CouponStoreDO{

    private Long orgId;
}
