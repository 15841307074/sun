package com.htyoudao.youdao.module.system.dal.dataobject.storebackground;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 门店背景模板 DO。
 *
 * 用于保存模板图片、应用范围、发布状态以及最后发布时间。
 */
@TableName("system_store_background_template")
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreBackgroundTemplateDO extends BusinessBaseDO {

    /** 门店背景模板编号。 */
    @TableId
    private Long backgroundId;
    /** 模板名称。 */
    private String templateName;
    /** 门店背景图片地址。 */
    private String backgroundImage;
    /** 应用范围：1 按门店，2 按标签。 */
    private Integer appScope;
    /** 门店范围：1 全部门店，2 部分门店。 */
    private Integer storeScope;
    /** 发布状态：0 关闭，1 发布。 */
    private Integer publishStatus;
    /** 是否为系统默认模板：0 否，1 是。 */
    private Integer defaultFlag;
    /** 最后发布时间。 */
    private LocalDateTime releaseTime;
}
