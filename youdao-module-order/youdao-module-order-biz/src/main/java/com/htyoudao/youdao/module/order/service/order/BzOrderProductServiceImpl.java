package com.htyoudao.youdao.module.order.service.order;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.BzOrderProductVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
@DS(DsNameConstants.SHARDING)
@Slf4j
@Service
public class BzOrderProductServiceImpl extends ServiceImpl<BzOrderProductMapper, BzOrderProductDO> implements BzOrderProductService {

    @Override
    public List<BzOrderProductVO> getOrderProductList(List<String> orderSns, LocalDateTime[] createTimes) {
        List<BzOrderProductDO> bzOrderProductDOS = baseMapper.selectList(orderSns, createTimes);
        return BeanCopyUtils.copyBeanList(bzOrderProductDOS, BzOrderProductVO.class);
    }

}
