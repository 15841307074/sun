package com.htyoudao.youdao.module.commodity.dal.dataobject;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.dubbo.common.logger.FluentLogger.S;

/**
 * 商品 同步任务 对象 commodity_sync_task
 * 
 * @author Qizhongnan
 * @date 2024-01-16
 */
@Getter
@Setter
@TableName("commodity_sync_task")
@EqualsAndHashCode(callSuper = true)
public class CommoditySyncTask extends BusinessBaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String batchNo;
    private Long storeId;
    private String storeName;
    private Integer status;
    private String params;
    private String requestData;
    private String syncData;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private String errorMessage;
    private Integer version;
}
