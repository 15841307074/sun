package com.htyoudao.youdao.module.commodity.dal.dataobject;


import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类对象 commodity_category
 *
 * @author Qizhongnan
 * @date 2024-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommodityCategory extends TimeBase {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 类型  是否下单必选分组 1 是 0否
     */

    private Integer type;

    /**
     * 分类名称
     */
    @NotBlank
    private String name;

    /**
     * 顺序
     */

    private Integer sort;

    /**
     * 分类状态 0:启用，1:禁用
     */

    private Integer status;

    /**
     * 是否隐藏 1是 0否
     */
    private Integer isHidden;

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
    @TableField(value = "commodity_ids")
    private String commoditySpusIds;






}
