package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "门店列表带字母索引VO")
@Data
public class StoreListWithIndexVO {

    @Schema(description = "字母索引列表（A-Z，#）")
    private List<String> letters;

    @Schema(description = "按字母分组的门店列表")
    private Map<String, List<StoreLettersRespVO>> groupedStores;

    @Schema(description = "扁平的门店列表（用于兼容）")
    private List<StoreLettersRespVO> storeList;
}
