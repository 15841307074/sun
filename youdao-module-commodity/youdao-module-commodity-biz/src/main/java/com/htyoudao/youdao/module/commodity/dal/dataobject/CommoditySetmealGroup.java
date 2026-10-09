package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐内容分组对象 commodity_setmeal_group
 * 
 * @author Qizhongnan
 * @date 2024-01-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommoditySetmealGroup extends BusinessBaseDO
{
    private static final long serialVersionUID = 1L;

    /** 主键，套餐内容分组ID */

    @TableId(value = "group_id",type= IdType.ASSIGN_ID)
    private Long groupId;

    /** 关联套餐ID 弃用*/

    private Long commoditySetmealId;



    /** 排序 */
    private int sort;

    /** 分组名称 */
    private String commodityGroupName;

    /** 必选商品数量 */
    private Long choose;

    /** 分组属性(本组商品是否包含以下所有单品) 1 可选 2 固定 3 加价组 */
    private int attribute;




    /** 分组的单品 ids 弃用 */
    private String commoditySingleIds;


    /**
     * 关联商品 Id
     */
    private Long commodityId;

    /**
     * 分组状态 1上 0 下
     */
    private Integer status;
    /**
     * 门店下套餐分组里可选的数量
     */
   // private Integer commodityStoreGroupChoose;
    /**
     * 同一商品是否可选多份 1 是 0否
     */
    private Integer chooseMany;

}
