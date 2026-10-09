package com.htyoudao.youdao.module.order.client.DTO;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SyncCommodityDTO {

    private String mainId;

    private String openId;

    private String sessionStr;
}
