package com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "供应链商品请求 VO")
public class ScmCommodityDataReqVO extends PageParam{


    @Schema(description = "商品名称/编号")
    private String commodityName;

}
