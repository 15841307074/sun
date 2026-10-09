package com.htyoudao.youdao.module.order.dal.DTO;

import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductSonDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderPurchaseDO;
import lombok.Data;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-08-30
 */
@Data
public class OrderDetailDTO {
    private BzOrderDO bzOrderDO;
    private List<BzOrderProductDO> productDOList;
    private List<BzOrderPurchaseDO> purchaseDOList;
    private List<BzOrderProductSonDO> productSonDOList;
}
