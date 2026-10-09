package com.htyoudao.youdao.module.order.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonRequest;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonResult;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductSonDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2024-10-08
 */
public interface BzOrderProductSonService extends IService<BzOrderProductSonDO> {

    List<BzOrderProductSonDO> getOrderProductSonList(List<Long> productIds, LocalDateTime[] createTimes);

    List<ProductSonResult> productSonList(ProductSonRequest sonRequest);
}
