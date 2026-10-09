package com.htyoudao.youdao.module.order.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderCondimentsDO;

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
public interface BzOrderCondimentsService extends IService<BzOrderCondimentsDO> {

    List<BzOrderCondimentsDO> getOrderCondimentsList(List<String> orderSns, LocalDateTime[] createTimes);
}
