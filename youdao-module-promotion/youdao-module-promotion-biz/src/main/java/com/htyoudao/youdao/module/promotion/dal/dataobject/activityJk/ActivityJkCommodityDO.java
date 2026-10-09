package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;


@TableName(value = "activity_jk_commodity", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityJkCommodityDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -5291058844334477585L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    // 集点活动ID
    private Long activityId;
    // 商品ID
    private Long commodityId;
    //商品名称
    private String commodityName;

}
