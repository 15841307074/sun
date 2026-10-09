package com.htyoudao.youdao.module.system.dal.dataobject.wxtemplateRelevance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;


@TableName("store_wecom_relevance")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class StoreWecomRelevanceDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 6855594069250478981L;
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模版id
     */
    private Long templateId;

    /**
     * 门店ID
     */
    private Long storeId;

}
