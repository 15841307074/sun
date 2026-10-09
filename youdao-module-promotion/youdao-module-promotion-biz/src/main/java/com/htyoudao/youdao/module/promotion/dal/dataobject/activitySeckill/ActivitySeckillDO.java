package com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;


@TableName(value = "activity_seckill", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySeckillDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059503L;

    // 秒杀ID（主键）
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long activityId;

    // 图片地址
    private String imageUrl;
    // 背景色（如#FFFFFF）
    private String backgroundColor;

    // 门店是否购买限制（0-否，1-是）
    private Integer isStoreLimit;
    // 每家店限购多少次
    private Integer storeLimitCount;
    // 分享设置（1-不允许转发至好友，2-允许转发至好友，3-允许复制链接至好友）
    private Integer shareSetting;
    // 是否显示弹幕开关（0-关，1-开）
    private Integer isBarrageShow;



    // 分享图片
    private String shareImageUrl;
    // 分享标题
    private String shareTitle;
    // 分享描述
    private String shareDescription;

    // 社群专享 1 不开启  2 开启
    private Integer communityFlag;

}
