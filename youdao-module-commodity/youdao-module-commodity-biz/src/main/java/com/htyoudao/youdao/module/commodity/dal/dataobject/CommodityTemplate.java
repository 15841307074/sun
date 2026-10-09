package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 商品模板对象 commodity_template
 *
 * @author Qizhongnan
 * @date 2024-03-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommodityTemplate extends BusinessBaseDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品模板ID
     */
    @TableId(value = "commodity_template_id", type = IdType.ASSIGN_ID)
    private Long commodityTemplateId;

    /**
     * 模板名称
     */
    private String commodityTemplateName;

    /**
     * 模板状态
     */
    private Long commodityTemplateStatus;
    /**
     * 模板描述
     */
    private String templateDesc;

    /**
     * 模板属性（1.允许门店自己管理，2.品牌方统一管理）
     */
    private Integer templateFlavor;

    /**
     * 是否只允许修改价格 1 是 2 否
     */
    private Integer choosePrice;


}
