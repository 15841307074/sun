package com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingfloat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.BaseEntity;
import lombok.Data;

/**
 * 浮窗位置
 */
@TableName("advertising_float")
@Data
public class AdvertisingFloatDO extends BusinessBaseDO {

   private Integer  floatingWindowDisplay; //浮窗显示页面 （1全选 2首页 3订单结算页 4点餐页 5订单详情页 6积分商城 7个人中心页）
    private Long advertisingId; //浮窗广告 id
}
