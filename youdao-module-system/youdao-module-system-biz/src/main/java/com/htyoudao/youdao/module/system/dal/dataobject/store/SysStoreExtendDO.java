package com.htyoudao.youdao.module.system.dal.dataobject.store;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 店铺关系表
 * </p>
 *
 * @author dingyunfei
 * @since 2024-05-14
 */
@Data
@TableName("sys_store_extend")
public class SysStoreExtendDO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3832893305948188578L;
    /**
     * 主键
     */
    @TableId(value = "extend_id", type = IdType.AUTO)
    private Integer extendId;

    /**
     * 商户id
     */
    private Long storeId;

    /**
     * 支付终端编号
     */
    private String terminalSn;

    /**
     * 支付secret
     */
    private String terminalKey;

    /**
     * 支付激活码
     */
    private String code;
}
