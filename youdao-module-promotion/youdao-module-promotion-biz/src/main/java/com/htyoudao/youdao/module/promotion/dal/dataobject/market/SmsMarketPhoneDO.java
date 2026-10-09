package com.htyoudao.youdao.module.promotion.dal.dataobject.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@Schema(name = "营销短信实体", description = "营销短信实体")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sms_template_phone")
public class SmsMarketPhoneDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "短信营销id")
    private Long smsTemplateId;

    @Schema(description = "电话号")
    private String phone;

    @Schema(description = "发送次数")
    private Integer times;
}
