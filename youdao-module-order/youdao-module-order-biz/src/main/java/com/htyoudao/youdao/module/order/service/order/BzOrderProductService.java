package com.htyoudao.youdao.module.order.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.BzOrderProductVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;

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
public interface BzOrderProductService extends IService<BzOrderProductDO> {

    List<BzOrderProductVO> getOrderProductList(List<String> orderSns, LocalDateTime[] createTimes);

}
