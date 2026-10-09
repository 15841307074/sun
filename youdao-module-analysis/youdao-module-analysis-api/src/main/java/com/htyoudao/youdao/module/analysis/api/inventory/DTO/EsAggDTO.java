package com.htyoudao.youdao.module.analysis.api.inventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsAggDTO {
    public LocalDateTime[] times;
    public List<Long> storeIds;
    public List<Integer> orderFroms;

    public List<Integer> orderS;

    /**
     * 是否会员 isSettlement  0否 1是
     */
    private Integer isSettlement;

    /**
     * 是否新客 expressId  0否 1是
     */
    private Integer expressId;

    private String cityName;

    private String storeName;

    private Long activityId;

    private Boolean selectNoShow = false;
    public Long storeId;

    public String memberText;

    private Long commodityId;

    private Integer channel;
}