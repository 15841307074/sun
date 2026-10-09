package com.htyoudao.youdao.module.promotion.controller.admin.market.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
public class SmsTemplateRespVO {
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板内容")
    private String templateContent;

    @Schema(description = "模板编号")
    private String templateCode;
}
