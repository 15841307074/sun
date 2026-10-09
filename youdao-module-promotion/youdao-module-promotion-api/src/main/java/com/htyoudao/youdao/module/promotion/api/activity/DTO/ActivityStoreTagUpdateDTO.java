package com.htyoudao.youdao.module.promotion.api.activity.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 门店标签变更后联动维护活动-门店绑定的入参
 */
@Data
public class ActivityStoreTagUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8020054352008344497L;

    @Schema(description = "门店编号")
    private Long storeId;

    @Schema(description = "变更的标签编号列表")
    private List<Long> tagIds;
}
