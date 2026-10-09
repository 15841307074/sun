package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;

@TableName("bz_order_log")
@Data
public class BzOrderLogDO  extends BusinessBaseDO {
    @Serial
    private static final long serialVersionUID = -8903426497462154112L;

    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    private Long logId;

    private Long logUserId;

    private String logUserName;

    private String orderSn;

    private Integer orderStateLog;

    private Date logTime;

    private String logContent;
}
