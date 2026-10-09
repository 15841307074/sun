package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-03
 */
@Data
@TableName("sys_pay_record")
public class SysPayRecordDO extends BusinessBaseDO {
    @Serial
    private static final long serialVersionUID = -1547128507967940278L;

    @TableId(value = "id")
    private Long id;

    private String orderId;

    private String thridOrderNo;

    private String methodParam;

    private String methodReturn;

    private String methodName;

    private String methodPostParam;

    private String methodPostResult;

    private String payUrl;

    private String payUrlResult;

    private String merchantId;

    private String agetId;

    private String custId;
}