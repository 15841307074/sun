package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 商品分组查询 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityTypeReqVo {


    @Schema(description = "查询逻辑 1全部 2上架 3下架 4隐藏",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "查询条件不能为空")
    private Integer selectView;
}
