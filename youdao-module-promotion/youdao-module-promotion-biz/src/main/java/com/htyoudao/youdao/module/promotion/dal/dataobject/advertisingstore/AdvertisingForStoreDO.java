package com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingstore;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.BaseEntity;
import lombok.Data;



@TableName("advertising_for_store")
@Data
public class AdvertisingForStoreDO extends BusinessBaseDO {
    private Long storeId;
    private Long advertisingId;
}
