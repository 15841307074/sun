package com.htyoudao.youdao.module.promotion.controller.admin.market.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author dht
 * 纯后端用
 */
@Data
@AllArgsConstructor
public class MarketUpdatedEvent {

    private List<String> phoneNumbers;

    private Long marketId;

    private Set<Long> memberIds;
}
