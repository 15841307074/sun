package com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore;

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
@TableName("activity_store")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityStoreDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 6855594069250478981L;
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
    private Long storeId;

}
