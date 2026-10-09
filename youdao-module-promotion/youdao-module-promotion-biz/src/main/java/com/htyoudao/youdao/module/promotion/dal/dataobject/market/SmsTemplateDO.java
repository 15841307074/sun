package com.htyoudao.youdao.module.promotion.dal.dataobject.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author dht
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sms_template")
public class SmsTemplateDO extends BusinessBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
