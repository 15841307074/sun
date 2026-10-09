package com.htyoudao.youdao.module.order.api.order;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.api.order.dto.BzOrderDTO;
import com.htyoudao.youdao.module.order.api.order.dto.OrderDetailRspDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author dht
 */
@Tag(name = "订单模块")
public interface BzOrderApi {

    /**
     * 获取昨天下单数据
     *
     * @param yesterdayStart yesterdayStart
     * @param yesterdayEnd   yesterdayEnd
     * @return CommonResult<BzOrderDTO>
     */
    List<BzOrderDTO> selectBzOrderStoreData(LocalDateTime yesterdayStart, LocalDateTime yesterdayEnd);

    /**
     * 获取昨天下单数据全部
     *
     * @param startTime startTime
     * @param endTime   endTime
     * @return CommonResult<BzOrderDTO>
     */
    List<BzOrderDTO> selectBzOrderData(LocalDateTime startTime, LocalDateTime endTime);


    /**
     * 获取昨天下单数据有效
     *
     * @param startTime startTime
     * @param endTime   endTime
     * @return CommonResult<BzOrderDTO>
     */
    List<BzOrderDTO> selectBzOrderData2(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取历史订单的电话号
     *
     * @param date date
     * @return BzOrderData
     */
    Set<Long> getHisPhones(String date, List<Long> storeIds);

    /**
     * 获取一周订单
     *
     * @param date      date
     * @param localDate localDate
     * @return String
     */
    Set<Long> getWeekPhones(String date, LocalDate localDate, List<Long> storeIds);

    /**
     * 获取订单详情
     */
    OrderDetailRspDTO getOrderDetail(@RequestParam("orderSn") String orderSn);

    /** 满赠库存补偿专用：仅查询订单状态，订单不存在时返回 null。 */
    Integer getOrderStateForMzInventory(@RequestParam("orderSn") String orderSn);

    /**
     * 重置活动和删除活动需要调用
     */
    void delOrderPointsByActivityId(Long activityId) throws IOException;

    /**
     * 检查跑腿员是否有未完结订单
     * @param memberId 会员ID（跑腿员对应的用户ID）
     * @return true-有未完结订单，false-没有未完结订单
     */
    boolean checkRunnerHasUnfinishedOrders(Long memberId);
}
