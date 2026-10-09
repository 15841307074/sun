package com.htyoudao.youdao.module.order.service.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.excel.pojo.SearchAfterPage;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.order.api.order.dto.BzOrderDTO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.*;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.BzOrderPrintInfoDTO;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.OrderCountByOrderTypeDTO;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.SettlementReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderHallPageRespVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderHallReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.ErrandOrderHallRespVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderPurchaseDO;
import com.htyoudao.youdao.module.order.dal.es.BzOrderPointsDocument;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 订单 Service 接口
 *
 * @author 0090
 */
public interface BzOrderService extends IService<BzOrderDO> {

    /**
     * 集点记录新增
     *
     * @param detail
     */
    void addOrderPoints(OrderDetailDTO detail);

    /**
     * 集点记录删除
     *
     * @param activityId
     */
    void delOrderPointsByActivityId(Long activityId) throws IOException;

    /**
     * 集点记录删除, 重置活动和删除活动需要调用
     *
     * @param orderId
     */
    void delOrderPointsByOrderId(Long orderId) throws IOException;

    /**
     * 分页查询
     *
     * @param reqVO
     * @return
     */
    SearchAfterPage<OrderResVO> pOrderpage(OrderPageReqVO reqVO);

    /**
     * 集点记录
     *
     * @param activityId
     * @return
     */
    List<BzOrderPointsDocument> pointsCollectPage(Long activityId);

    /**
     * 集点点数
     *
     * @param activityId
     * @return
     */
    Integer pointsCollectCount(Long activityId);

    /**
     * 订单详情
     */
    OrderDetailRspVO getBzOrderInfo(String orderSn);

    /**
     * 小程序订单列表
     *
     * @param reqVO
     * @return
     */
    Page<BzOrderAppListRspVO> cOrderPage(BzOrderAppListReqVO reqVO);

    /**
     * app订单列表
     *
     * @param reqVO
     * @return
     */
    PageResult<BzOrderAppListRspVO> appOrderPage(OrderPageReqVO reqVO);

    /**
     * 导出用户
     *
     * @param exportReqVO 导出条件
     * @param response    响应
     * @throws IOException IOException
     */
    void exportOrderList(OrderPageReqVO exportReqVO, HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 点餐机列表查询
     *
     * @param reqVO
     * @return
     */
    Page<BzOrderListRspVO> orderPage(BzOrderReqVO reqVO);

    /**
     * 根据订单号查询订单
     *
     * @param orderSn
     * @return
     */
    BzOrderDO getBzOrderDO(String orderSn);

    /** 仅查询订单状态；订单不存在时返回 null，供满赠过期库存对账使用。 */
    Integer getOrderStateOrNull(String orderSn);

    /**
     * 查询三天内订单
     */
    List<String> selectOrderByMemberIdAndTrhDay(Long memberId, Long storeId);

    /**
     * 餐机统计堂食，外卖订单数量
     *
     * @param reqVO
     * @return
     */
    List<OrderCountByOrderTypeDTO> selectCountByOrderType(BzOrderReqVO reqVO);

    /**
     * 推送
     *
     * @param orderSn
     */
    void changeToGetting(String orderSn);

    /**
     * 结算
     *
     * @param reqVO
     * @return
     */
    CalculateCacheDataDTO calculate(SettlementReqVO reqVO);

    /**
     * 结算
     *
     * @param reqVO
     * @return
     */
    CalculateCacheDataV2DTO calculateV2(SettlementReqV2VO reqVO);

    /**
     * 秒杀结算
     *
     * @param reqVO
     * @return
     */
    CalculateCacheDataV2DTO seckillCalculateV2(SettlementReqV2VO reqVO);

    /**
     * 提交订单
     *
     * @param reqVO
     * @return
     */
    SubmitResVO submitOrder(SubmitReqVO reqVO);

    /**
     * 秒杀提交订单
     *
     * @param reqVO
     * @return
     */
    SubmitResVO seckillSubmit(SubmitReqVO reqVO);

    /**
     * 获取取餐码
     *
     * @param storeId
     * @return
     */
    String getPickupCode(Long storeId);

    /**
     * 获取外卖订单号
     *
     * @param storeId
     * @return
     */
    String getWmPickupCode(Long storeId);

    /**
     * 获取代取订单号
     *
     * @param storeId
     * @return
     */
    String getDqPickupCode(Long storeId);

    /**
     * 更新订单状态
     *
     * @param bzOrderDO
     */
    void updateOrderState(BzOrderDO bzOrderDO);

    /**
     * 核销订单
     *
     * @param orderSn
     * @param storeId
     */
    void writeOff(String orderSn, Long storeId);

    /**
     * 订单完成
     *
     * @param orderSn
     */
    void completed(String orderSn);

    /**
     * 叫号取餐
     *
     * @param orderSn
     */
    void callNumber(String orderSn);

    /**
     * 小票扫码
     *
     * @param orderSn
     */
    void receiptScan(String orderSn);

    /**
     * 配送
     *
     * @param orderSn
     */
    void delivery(String orderSn);

    /**
     * 取消订单
     *
     * @param orderSn
     */
    void cannel(String orderSn);

    /**
     * 取消优惠券时间任务
     *
     * @param bzOrderDO
     */
    void cancelCouponTimeTask(BzOrderDO bzOrderDO, Integer timeOut);

    /**
     * 添加订单时间任务
     *
     * @param bzOrderDO
     */
    void addOrderTimerTask(BzOrderDO bzOrderDO, Integer timeOut);

    /**
     * 添加代取订单待接单超时取消时间任务
     *
     * @param bzOrderDO
     */
    void addErrandWaitingAcceptCancelTimerTask(BzOrderDO bzOrderDO);

    /**
     * 已支付待接单代取订单取消并退款
     *
     * @param order 订单
     */
    void cancelWaitingAcceptErrandOrderAndRefund(BzOrderDO order);

    /**
     * 已接单或配送中但未确认收货的代取订单自动确认收货
     *
     * @param order 订单
     */
    void completeAcceptedErrandOrder(BzOrderDO order);

    /**
     * 扫描并取消当天已支付待接单的代取订单
     */
    void cancelTodayWaitingAcceptErrandOrders();

    /**
     * 扫描并完成当天已接单或配送中但未确认收货的代取订单
     */
    void completeTodayAcceptedErrandOrders();

    /**
     * 更新订单完成时间
     *
     * @param memberId
     * @param bzOrderDO
     */
    void updateFinalOrderFinishTime(Long memberId, BzOrderDO bzOrderDO);

    /**
     * 获取昨天下单数据 WxMember定时任务用
     *
     * @param yesterdayStart yesterdayStart
     * @param yesterdayEnd   yesterdayEnd
     * @return List<BzOrderDTO>
     */
    List<BzOrderDTO> selectBzOrderStoreData(LocalDateTime yesterdayStart, LocalDateTime yesterdayEnd);

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
     * 获取打印订单统计小票
     *
     * @param storeId storeId
     * @return BzOrderPrintInfoDTO
     */
    BzOrderPrintInfoDTO getOrderPrintInfo(Long storeId);

    /**
     * 获取订单信息，包括商品、加购、子商品
     *
     * @param orderSn
     * @return
     */
    OrderDetailDTO getDetail(String orderSn);

    /**
     * 增加商品销量
     *
     * @param productDOList
     * @param isRefund
     */
    void incrementProductSales(List<BzOrderProductDO> productDOList, Boolean isRefund);

    /**
     * 增加加购商品销量
     *
     * @param purchaseDOList
     * @param isRefund
     */
    void incrementPurchaseSales(List<BzOrderPurchaseDO> purchaseDOList, Boolean isRefund);

    /**
     * 增加商品销量
     *
     * @param commodityId
     * @param count
     * @param isRefund
     */
    void _incrementProductSales(Long commodityId, long count, Boolean isRefund);

    /**
     * 增加加购商品销量
     *
     * @param commodityId
     * @param isRefund
     */
    void _incrementPurchaseSales(Long commodityId, Boolean isRefund);

    /**
     * 获取某日订单全部
     *
     * @param startTime startTime
     * @param endTime   endTime
     * @return List<BzOrderDTO>
     */
    List<BzOrderDTO> selectBzOrderData(LocalDateTime startTime, LocalDateTime endTime);


    /**
     * 获取某日订单有效
     *
     * @param startTime startTime
     * @param endTime   endTime
     * @return List<BzOrderDTO>
     */
    List<BzOrderDTO> selectBzOrderData2(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 订单库存变更
     *
     * @param bzOrderDO
     * @param detail
     * @param stockChangeEnum
     */
    void changeStock(BzOrderDO bzOrderDO, OrderDetailDTO detail, StockChangeEnum stockChangeEnum);

    /**
     * 订单mq
     *
     * @param orderSn
     * @param type
     */
    void notifyOrder(String orderSn, String type) throws Exception;

    /**
     * 订单打印
     *
     * @param orderSn
     * @param startTime
     * @param endTime
     * @return
     */
    void putProductEs(String orderSn, String startTime, String endTime);

    /**
     * 跑腿员接单
     *
     * @param orderSn 订单号
     */
    void acceptErrandOrder(String orderSn);

    /**
     * 跑腿员已取货
     *
     * @param orderSn 订单号
     */
    void pickupErrandOrder(String orderSn);

    /**
     * 跑腿员我已送达
     *
     * @param orderSn        订单号
     * @param deliveryImages 送达照片，最多3张
     */
    void deliveredErrandOrder(String orderSn, List<String> deliveryImages);

    /**
     * 跑腿接单大厅
     *
     * @param reqVO 请求参数
     * @return 待接单列表
     */
    ErrandOrderHallPageRespVO errandOrderHall(ErrandOrderHallReqVO reqVO);

    /**
     * 我的跑腿配送订单。
     *
     * @return 当前跑腿员接过的订单
     */
    ErrandOrderHallPageRespVO myErrandDeliveryOrders(ErrandOrderHallReqVO reqVO);


    /**
     * 检查跑腿员是否有未完结订单
     * @param memberId 会员ID（跑腿员对应的用户ID）
     * @return true-有未完结订单，false-没有未完结订单
     */
    boolean checkRunnerHasUnfinishedOrders(Long memberId);
}
