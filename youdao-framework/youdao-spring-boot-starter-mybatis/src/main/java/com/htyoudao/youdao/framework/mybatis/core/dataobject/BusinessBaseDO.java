package com.htyoudao.youdao.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拓展多项目的 BaseDO 基类
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BusinessBaseDO extends BaseDO {

    /**
     * 项目 id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long businessId;

}
