package com.htyoudao.youdao.module.promotion.dal.dataobject.activitySign;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "activity_sign", autoResultMap = true)
public class ActivitySignDO extends BusinessBaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private String activityCoverImage;
    private String activityBackgroundImage;
    private String unsignedImage;
    private String signedImage;
    private String activityDetailImage;
    private String themeColor;
    private Integer shareType;
    private String shareTitle;
    private String shareNote;
    private String shareImgUrl;
    private Integer resetEnabled;
    private Integer resetType;
    private String resetDays;
    private Integer continuousCycleDays;
    private Integer communityOnly;
    private String storeManagerQrCodeGuideImage;
    private String storeGroupQrCodeGuideImage;
    private String activityRule;
}
