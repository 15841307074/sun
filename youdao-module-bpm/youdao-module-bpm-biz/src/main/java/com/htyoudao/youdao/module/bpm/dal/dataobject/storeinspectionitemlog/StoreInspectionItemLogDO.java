package com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionitemlog;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 巡店项操作变更日志 DO
 *
 * @author 超级管理员
 */
@TableName(value = "system_store_inspection_item_log", autoResultMap = true)
@KeySequence("system_store_inspection_item_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInspectionItemLogDO extends BusinessBaseDO {

    /**
     * 日志ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 所属巡店记录ID
     */
    private Long recordId;
    /**
     * 所属巡店明细项ID
     */
    private Long recordItemId;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 操作人姓名
     */
    private String operatorName;
    /**
     * 操作时间
     */
    private LocalDateTime operateTime;
    /**
     * 变更前数据快照
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> beforeSnap;
    /**
     * 变更后数据快照
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> afterSnap;
    /**
     * 修改原因备注
     */
    private String remark;

}