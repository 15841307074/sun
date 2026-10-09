package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD;


import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDCommoditySaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDCouponPackageReqSaveVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDCouponReqSaveVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@TableName(value = "activity_jd", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityJDDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 6863443806222411584L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String activityName;

    private String bgImageUrl;

    private String backgroundColor;

    private String collectPointsImageUrl;

    private String collectedPointsImageUrl;

    private Integer collectPointsType;

    private Integer collectPointsThreshold;

    private Integer collectPointsCommodityType;

    private Integer validityPeriod;

    private Integer distributeMode;

    private Long activityId;

    // 分享图片
    private String shareImageUrl;
    // 分享标题
    private String shareTitle;
    // 分享描述
    private String shareDescription;

    private String shareSetting;

    // 社群专享 1 不开启  2 开启
    private Integer communityFlag;

}
