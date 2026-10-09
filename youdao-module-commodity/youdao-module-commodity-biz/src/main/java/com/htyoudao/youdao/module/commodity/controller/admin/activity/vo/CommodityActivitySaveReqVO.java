package com.htyoudao.youdao.module.commodity.controller.admin.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 商品活动新增/修改 Request VO")
@Data
public class CommodityActivitySaveReqVO {

    @Schema(description = "商品表-商品唯一标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "22250")
    @NotNull(message = "商品表-商品唯一标识符不能为空")
    private List<Long> commodityIds;

}