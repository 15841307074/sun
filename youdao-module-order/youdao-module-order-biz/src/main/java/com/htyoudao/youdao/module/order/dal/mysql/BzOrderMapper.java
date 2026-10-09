package com.htyoudao.youdao.module.order.dal.mysql;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.api.order.dto.BzOrderDTO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.BzOrderReqVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.OrderPageReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.OrderCountByOrderTypeDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import com.htyoudao.youdao.module.order.enums.OrderTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Mapper
public interface BzOrderMapper extends BaseMapperX<BzOrderDO> {

    /**
     * 分页查询
     *
     * @param reqVO reqVO
     * @return PageResult<OrgDO>
     */
    @DS(DsNameConstants.SHARDING)
    default PageResult<BzOrderDO> selectPage(OrderPageReqVO reqVO) {
        LambdaQueryWrapperX<BzOrderDO> wrapper = new LambdaQueryWrapperX<BzOrderDO>()
                .likeIfPresent(BzOrderDO::getOrderSn, reqVO.getOrderSn())
                .likeIfPresent(BzOrderDO::getPaySn, reqVO.getPaySn())
                .inIfPresent(BzOrderDO::getMemberId, reqVO.getMemberId())
                .inIfPresent(BzOrderDO::getOrderType, reqVO.getOrderTypeQueryList())
                .eqIfPresent(BzOrderDO::getPaymentCode, reqVO.getPaymentCode())
                .eqIfPresent(BzOrderDO::getOrderFrom, reqVO.getOrderFrom())
                .betweenIfPresent(BzOrderDO::getCreateTime, reqVO.getStartOrderTime(), reqVO.getEndOrderTime())
                .orderByDesc(BzOrderDO::getCreateTime);
        List<Integer> orderStates = reqVO.getOrderStateQueryList();
        if (orderStates == null || orderStates.isEmpty()) {
            wrapper.and(w -> w.ne(BzOrderDO::getOrderType, OrderTypeEnum.ERRAND.getCode())
                    .or()
                    .notIn(BzOrderDO::getOrderState, OrderStateEnum.UNPAID.getCode(), OrderStateEnum.WAITING_ACCEPT.getCode()));
        } else if (orderStates.stream().anyMatch(java.util.Objects::nonNull)) {
            wrapper.and(w -> {
                boolean first = true;
                for (Integer orderState : orderStates) {
                    if (orderState == null) {
                        continue;
                    }
                    if (!first) {
                        w.or();
                    }
                    first = false;
                    w.eq(BzOrderDO::getOrderState, orderState);
                }
            });
        }
        return selectPage(reqVO, wrapper);
    }

    @DS(DsNameConstants.MASTER)
    List<BzOrderDO> selectValidListByStoreIdAndCreateTime(@Param("tableFix") String tableFix,
                                                           @Param("storeId") Long storeId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 分页查询
     *
     * @param page   page
     * @param record record
     * @return PageResult<BzOrderDO>
     */
    Page<BzOrderDO> orderPage(Page page, @Param("record") BzOrderReqVO record);


    /**
     * 根据会员id和日期查询订单
     *
     * @param memberId       memberId
     * @param storeId        storeId
     * @param startOrderTime startOrderTime
     * @param endOrderTime   endOrderTime
     * @return List<String>
     */
    List<String> selectOrderByMemberIdAndTrhDay(@Param("tableFixs") List<String> tableFixs, @Param("memberId") Long memberId, @Param("storeId") Long storeId, @Param("startOrderTime") String startOrderTime, @Param("endOrderTime") String endOrderTime);

    /**
     * 订单量聚合
     *
     * @param record
     * @return
     */
    List<OrderCountByOrderTypeDTO> selectCountByOrderType(@Param("record") BzOrderReqVO record);

    /**
     * 更新订单状态
     *
     * @param bzOrderDO
     */
    void updateOrderState(@Param("r") BzOrderDO bzOrderDO);

    /**
     * 根据订单编号查询订单，指定分表。
     *
     * @param tableFix 表后缀
     * @param orderSn  订单号
     * @return 订单
     */
    BzOrderDO selectByOrderSnByTable(@Param("tableFix") String tableFix, @Param("orderSn") String orderSn);

    /**
     * 查询当天已支付待接单的代取订单。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 订单列表
     */
    List<BzOrderDO> selectWaitingAcceptErrandOrders(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询当天已接单或配送中但未确认收货的代取订单。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 订单列表
     */
    List<BzOrderDO> selectAcceptedErrandOrders(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 根据订单ID修改订单
     *
     * @param record
     */
    void updateByOrderId(@Param("tableFix") String tableFix, @Param("record") BzOrderDO record);

    /**
     * 跑腿员接单，按待接单状态做条件更新，避免并发重复接单。
     *
     * @param record 订单更新信息
     * @return 更新行数
     */
    int acceptErrandOrder(@Param("record") BzOrderDO record,
                          @Param("waitingAcceptOrderState") Integer waitingAcceptOrderState);

    /**
     * 已支付待接单代取订单超时取消退款，指定分表更新。
     *
     * @param record                  订单更新信息
     * @param waitingAcceptOrderState 待接单状态
     * @return 更新行数
     */
    int refundWaitingAcceptErrandOrder(@Param("record") BzOrderDO record,
                                       @Param("waitingAcceptOrderState") Integer waitingAcceptOrderState);

    /**
     * 跑腿员更新跑腿订单状态，按跑腿员和当前状态做条件更新。
     *
     * @param record             订单更新信息
     * @param deliveryId         跑腿员会员ID
     * @param expectedOrderState 期望当前跑腿状态
     * @return 更新行数
     */
    int updateErrandOrderStatus(@Param("record") BzOrderDO record,
                                @Param("deliveryId") Long deliveryId,
                                @Param("expectedOrderState") Integer expectedOrderState);

    /**
     * 获取昨天订单数据
     *
     * @param yesterdayStart yesterdayStart
     * @param yesterdayEnd   yesterdayEnd
     * @return List<BzOrderDTO>
     */
    List<BzOrderDTO> selectBzOrderStoreData(@Param("yesterdayStart") LocalDateTime yesterdayStart, @Param("yesterdayEnd") LocalDateTime yesterdayEnd);

    /**
     * 获取昨天订单数据 全部
     *
     * @param yesterdayStart yesterdayStart
     * @param yesterdayEnd   yesterdayEnd
     * @return List<BzOrderDTO>
     */
    List<BzOrderDTO> selectBzOrderData(@Param("yesterdayStart") LocalDateTime yesterdayStart, @Param("yesterdayEnd") LocalDateTime yesterdayEnd);

    /**
     * 获取昨天订单数据 有效
     *
     * @param yesterdayStart yesterdayStart
     * @param yesterdayEnd   yesterdayEnd
     * @return List<BzOrderDTO>
     */
    List<BzOrderDTO> selectBzOrderData2(@Param("yesterdayStart") LocalDateTime yesterdayStart, @Param("yesterdayEnd") LocalDateTime yesterdayEnd);


    /**
     * 获取历史订单的电话号
     *
     * @param date     date
     * @param storeIds storeIds
     * @return Set<Long>
     */
    Set<Long> getHisPhones(@Param("date") String date, @Param("storeIds") List<Long> storeIds);

    /**
     * 获取一周订单
     *
     * @param date      date
     * @param localDate localDate
     * @param storeIds  storeIds
     * @return Set<Long>
     */
    Set<Long> getWeekPhones(@Param("date") String date, @Param("localDate") LocalDate localDate, @Param("storeIds") List<Long> storeIds);
}
