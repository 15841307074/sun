package com.htyoudao.youdao.module.commodity.service.storeSpu.strategy;

import com.htyoudao.youdao.module.commodity.enums.ClientType;
import java.util.Map;

public class CommodityStrategyFactory {
    private static final Map<ClientType, CommodityClientStrategy> strategies = Map.of(
        ClientType.WX, new WxClientStrategy(),
        ClientType.DC, new DcClientStrategy(),
        ClientType.DC_FILTER, new DcFilterClientStrategy()
    );

    public static CommodityClientStrategy getStrategy(ClientType type) {
        return strategies.get(type);
    }
}