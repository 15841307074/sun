package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 抽卡记录VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "抽卡记录")
public class DrawRecordVO extends BusinessBaseDO implements Serializable {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "卡片ID")
    private Long cardId;

    @Schema(description = "卡片名称")
    private String cardName;

    @Schema(description = "卡片类型")
    private Integer cardType;

    @Schema(description = "卡片图片")
    private String cardImgUrl;

    @Schema(description = "抽卡时间")
    private Date drawTime;

    @Schema(description = "是否稀有卡")
    private Boolean isRare;
}
