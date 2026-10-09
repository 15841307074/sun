package com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("commodity_tag")
@EqualsAndHashCode(callSuper = true)
public class CommodityTag extends BusinessBaseDO {


    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String style;

    private String image;

}
