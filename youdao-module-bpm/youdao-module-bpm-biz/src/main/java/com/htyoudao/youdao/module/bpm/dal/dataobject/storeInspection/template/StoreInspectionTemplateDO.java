package com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 巡店模板 DO
 *
 * @author 超级管理员
 */
@TableName("system_store_inspection_template")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInspectionTemplateDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId(value = "template_id", type = IdType.ASSIGN_ID)
    private Long templateId;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 可见类型 0 全部可见 1 指定成员可见
     */
    private Integer visibilityType;

}