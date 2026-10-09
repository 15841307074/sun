package com.htyoudao.youdao.module.member.dal.dataobject.crowd;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 人群标签关系 DO
 *
 * @author 芋道源码
 */
@TableName("crowd_tag")
@KeySequence("crowd_tag_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrowdTagDO implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 标签id
     */
    private Long tagId;

    private String tagName;


    /**
     * 人群id
     */
    private Long crowdId;


}