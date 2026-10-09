package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 套餐内容分组对象 commodity_setmeal_group
 * 
 * @author Qizhongnan
 * @date 2024-01-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommodityTemplateSetmealGroup extends BusinessBaseDO
{
    private static final long serialVersionUID = 1L;

    /** 主键，套餐内容分组ID */

    @TableId(value = "id",type= IdType.ASSIGN_ID)
    /**
     * 主键，套餐内容分组ID
     */
    private Long id;

    /**
     * 主键，套餐内容分组ID
     */
    private Long groupId;

    /**
     * 关联套餐ID（弃用）
     */
    private Long commoditySetmealId;

    /**
     * 套餐里的单品 ids（弃用）
     */
    private String commoditySingleIds;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 分组名称
     */
    private String commodityGroupName;

    /**
     * 必选商品数量
     */
    private Integer choose;

    /**
     * 分组属性(本组商品是否包含以下所有单品) 1 是 2 不是（弃用）
     */
    private Integer attribute;




    /**
     * 商品Id
     */
    private Long commodityId;

    /**
     * 状态 1 是 0否
     */
    private Integer status;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板商品Id
     */
    private Long commodityTemplateId;


    private Integer chooseMany;

}
