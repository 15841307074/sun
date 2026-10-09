package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-17
 */
@Builder
@Data
@TableName(value = "ylb_order", autoResultMap = true)
public class YlbOrderDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderSn;
    private String req;
    private String rsp;
    private Date createTime;
}
