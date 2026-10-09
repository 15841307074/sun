package com.htyoudao.youdao.module.commodity.controller.admin.spuTag.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SelectCommodityTagListVO {

   @Schema(description = "名称")
   private String name;
}
