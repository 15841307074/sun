package com.htyoudao.youdao.module.member.dal.dataobject.crowd;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

/**
 * 标签门店关系 DO
 *
 * @author 芋道源码
 */
@TableName("crowd_store")
@KeySequence("crowd_store_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrowdStoreDO implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 门店id
     */
    private Long storeId;
    /**
     * 人群id
     */
    private Long crowdId;

    private String storeName;
}