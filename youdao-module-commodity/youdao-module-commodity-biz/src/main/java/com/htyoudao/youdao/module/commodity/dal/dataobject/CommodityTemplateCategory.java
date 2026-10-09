package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 模板商品分类
 * </p>
 *
 * @author wangwei
 * @since 2025-02-05
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName("commodity_template_category")
@EqualsAndHashCode(callSuper = true)
public class CommodityTemplateCategory extends TimeBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板id
     */
    private Long templateId;
    /**
     * 分类id
     */
    private Long  categoryId;

    /**
     * （原：类型   1 菜品分类 2 套餐分类）（分组属性 现：是否下单必选分组 1 是 0否）
     */
    private Integer type;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 顺序
     */
    private Integer sort;

    /**
     * 分类状态 0:禁用，1:启用
     */
    private Integer status;

    /**
     * 图片地址
     */
    private String url;


    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 分类里记录所有分类下商品的 ids
     */
    private String commodityIds;


}
