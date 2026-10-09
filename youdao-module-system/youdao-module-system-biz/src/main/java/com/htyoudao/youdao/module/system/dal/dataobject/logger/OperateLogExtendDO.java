package com.htyoudao.youdao.module.system.dal.dataobject.logger;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 操作日志扩展表，用于存储请求的详细信息
 *
 * @author 0090
 */
@TableName(value = "system_operate_log_extend", autoResultMap = true)
@KeySequence("system_operate_log_extend_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OperateLogExtendDO extends BaseDO {

    /**
     * 扩展日志主键
     */
    @TableId
    private Long id;

    /**
     * 主操作日志编号
     * 关联 {@link OperateLogDO#getId()}
     */
    private Long logId;

    /**
     * 请求参数
     * 记录请求的参数信息，JSON 格式
     */
    private String requestParams;

    /**
     * 请求头信息
     * 记录请求的 Header 信息，JSON 格式
     */
    private String requestHeaders;

}
