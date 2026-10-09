package com.htyoudao.youdao.module.system.dal.dataobject.sysconfig;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 云喇叭门店配置
 */
@Data
@TableName("ylb_store_config")
public class YlbStoreConfigDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2173376206379650270L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 注册开关 0关 1开
     */
    private Integer reqState;

    /**
     * 推送订单开关 0关 1开
     */
    private Integer pushState;
}

