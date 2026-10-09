package com.htyoudao.youdao.module.promotion.dal.dataobject.activityStoreTag;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

/**
 * @author villky
 * 营销活动关联门店表
 */
@TableName("activity_store_tag")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityStoreTagDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -8289680712903915132L;
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 门店ID
     */
    private Long tagId;

}
