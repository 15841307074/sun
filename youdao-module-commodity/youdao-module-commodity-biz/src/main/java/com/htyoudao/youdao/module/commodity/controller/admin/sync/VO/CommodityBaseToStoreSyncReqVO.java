package com.htyoudao.youdao.module.commodity.controller.admin.sync.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommodityBaseToStoreSyncReqVO {


    @Schema(description = "同步到的门店ID集合 key:id value:name")
    @NotEmpty(message = "没有门店信息")
    private Map<Long, String> storeIds;


    @Schema(description = "需要同步的商品id集合")
    @NotEmpty(message = "没有商品信息")
    private List<Long> commodityStoreSpuIds;


    @Schema(description = "模板属性（1.允许门店自己管理，2.品牌方统一管理）")
    private Integer templateFlavor;


    @Schema(description = "是否只允许修改价格 1 是 2 否")
    private Integer choosePrice;

}
