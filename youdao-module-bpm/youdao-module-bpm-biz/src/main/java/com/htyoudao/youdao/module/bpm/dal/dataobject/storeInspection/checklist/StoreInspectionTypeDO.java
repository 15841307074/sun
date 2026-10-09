package com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.checklist;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 巡店点检项目分类 DO
 *
 * @author 超级管理员
 */
@TableName("system_store_inspection_type")
@KeySequence("system_store_inspection_type_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInspectionTypeDO extends BusinessBaseDO {

    /**
     * 点检项目大类ID
     */
    @TableId
    private Long typeId;
    /**
     * 名称
     */
    private String typeName;

}