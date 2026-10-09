package com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingtime.AdvertisingTimeDO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Schema(description = "小程序 - 广告关联标签 Response VO")
@Data
public class AdvertisingTagRespVO {

    private Long tagId;


    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tagName;

}
