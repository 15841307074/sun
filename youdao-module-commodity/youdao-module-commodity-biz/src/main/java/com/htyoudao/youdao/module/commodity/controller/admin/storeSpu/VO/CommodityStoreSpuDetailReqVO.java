package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.TableField;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 门店详情 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityStoreSpuDetailReqVO {


    @Schema(description = "门店下商品的唯一ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSpuId;

}
