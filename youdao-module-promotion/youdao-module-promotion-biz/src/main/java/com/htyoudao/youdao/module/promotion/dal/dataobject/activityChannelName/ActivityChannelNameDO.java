package com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannelName;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

/**
 * 营销活动渠道名称表（activity_channel_name）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_channel_name")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityChannelNameDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 营销活动名称（渠道名称）
     */
    private String name;

    /**
     * 是否启用 1是 0否
     */
    private Integer isEnable;

    /**
     * 渠道链接（备用）
     */
    private String channelLink;

    /**
     * 是否默认 1是 0否
     */
    private Integer isDefault;
}

