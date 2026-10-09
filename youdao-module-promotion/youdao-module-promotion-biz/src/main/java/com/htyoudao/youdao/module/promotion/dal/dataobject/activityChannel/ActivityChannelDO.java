package com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_channel")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityChannelDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059503L;

    // 主键ID
    @TableId(type = IdType.AUTO)
    private Integer id;
    // 活动ID（关联秒杀活动表）
    private Long activityId;
    // 渠道名称
    private String channelName;
    //渠道 ID
    private Long channelId;

    //长链
    private String longUrl;
    // 链接地址
    private String linkUrl;
    //是否默认 1是 0否
    private Integer isDefault;
    /**
     * 是否启用 1是 0否
     */
    private Integer isEnable;
}
