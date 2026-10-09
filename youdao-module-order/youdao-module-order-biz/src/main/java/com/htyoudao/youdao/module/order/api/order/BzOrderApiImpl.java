package com.htyoudao.youdao.module.order.api.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.order.api.order.dto.BzOrderDTO;
import com.htyoudao.youdao.module.order.api.order.dto.OrderDetailRspDTO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.OrderDetailRspVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderProductMapper;
import com.htyoudao.youdao.module.order.service.order.BzOrderService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author dht
 */
@DubboService
public class BzOrderApiImpl implements BzOrderApi{

    @Resource
    private BzOrderService bzOrderService;

    @Resource
    private BzOrderProductMapper bzOrderProductMapper;

    @Override
    public List<BzOrderDTO> selectBzOrderStoreData(LocalDateTime yesterdayStart, LocalDateTime yesterdayEnd) {
        return bzOrderService.selectBzOrderStoreData(yesterdayStart,yesterdayEnd);
    }

    @Override
    public List<BzOrderDTO> selectBzOrderData(LocalDateTime startTime, LocalDateTime endTime) {
        return bzOrderService.selectBzOrderData(startTime,endTime);
    }

    @Override
    public List<BzOrderDTO> selectBzOrderData2(LocalDateTime startTime, LocalDateTime endTime) {
        return bzOrderService.selectBzOrderData2(startTime,endTime);
    }

    @Override
    public Set<Long> getHisPhones(String date, List<Long> storeIds) {
        return bzOrderService.getHisPhones(date,storeIds);
    }

    @Override
    public Set<Long> getWeekPhones(String date, LocalDate localDate, List<Long> storeIds) {
        return bzOrderService.getWeekPhones(date,localDate,storeIds);
    }
    @Override
    public OrderDetailRspDTO getOrderDetail(String orderSn) {
        OrderDetailRspVO orderDetailRspVO = bzOrderService.getBzOrderInfo(orderSn);
        return BeanUtils.toBean(orderDetailRspVO, OrderDetailRspDTO.class);
    }

    @Override
    public Integer getOrderStateForMzInventory(String orderSn) {
        return bzOrderService.getOrderStateOrNull(orderSn);
    }

    @Override
    public void delOrderPointsByActivityId(Long activityId) throws IOException {
        bzOrderService.delOrderPointsByActivityId(activityId);
    }

    @Override
    public boolean checkRunnerHasUnfinishedOrders(Long memberId) {
        return bzOrderService.checkRunnerHasUnfinishedOrders(memberId);
    }
}
