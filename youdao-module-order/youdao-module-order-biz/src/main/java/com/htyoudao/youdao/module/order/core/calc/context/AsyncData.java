package com.htyoudao.youdao.module.order.core.calc.context;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.framework.common.util.number.NumberUtils;
import com.htyoudao.youdao.module.commodity.api.DTO.AfterInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSingleInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.order.core.submit.DTO.ActivityBaseDetailDTO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.service.activity.calc.Activity;
import com.htyoudao.youdao.module.order.service.activity.dto.AllocationResult;
import com.htyoudao.youdao.module.order.service.activity.dto.ProductResult;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityBaseDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMJDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMzDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_GET_STORE_FAIL;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_STORE_NOT_OPEN;

/**
 * 异步数据
 *
 * @author zhangjihe
 * @since 2025-04-28
 */
@Slf4j
public record AsyncData(StoreDTO store,
                        Map<Long, StoreSkuInfoDTO> skuMap,
                        List<StoreSkuInfoDTO> skus,
                        Map<Long, AfterInfoDTO> afters,
                        Map<Long, StoreSingleInfoDTO> singles,
                        List<ProductResult> productResults,
                        Map<Long, List<ActivityMzDTO>> mzActivities
) {
}
