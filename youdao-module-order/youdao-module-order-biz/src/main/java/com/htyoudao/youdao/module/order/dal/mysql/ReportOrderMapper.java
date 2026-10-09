package com.htyoudao.youdao.module.order.dal.mysql;

import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportSelectRespVO;
import com.htyoudao.youdao.module.order.dal.DTO.OrderReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportOrderMapper {

    /**
     * 【供应链-总体-发货线】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportTotalFHX(@Param("tableFix0") String tableFix0,
                                           @Param("tableFix1") String tableFix1,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime,
                                           @Param("numberType") Integer numberType);

    /**
     * 【供应链-总体-配送线】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportTotalPSX(@Param("tableFix0") String tableFix0,
                                           @Param("tableFix1") String tableFix1,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime,
                                           @Param("numberType") Integer numberType);

    /**
     * 【供应链-总体-外地发货线】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportTotalWDX(@Param("tableFix0") String tableFix0,
                                           @Param("tableFix1") String tableFix1,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime,
                                           @Param("numberType") Integer numberType);

    /**
     * 【供应链-总体-220】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportTotal220(@Param("tableFix0") String tableFix0,
                                           @Param("tableFix1") String tableFix1,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime,
                                           @Param("numberType") Integer numberType);

    /**
     * 【供应链-总体-总部线】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportTotalZBX(@Param("tableFix0") String tableFix0,
                                           @Param("tableFix1") String tableFix1,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime,
                                           @Param("numberType") Integer numberType);

    /**
     * 【供应链-总体-浪大勺】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportTotalLDS(@Param("tableFix0") String tableFix0,
                                           @Param("tableFix1") String tableFix1,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime,
                                           @Param("numberType") Integer numberType);

    /**
     * 【供应链-仓库】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportWarehouse(@Param("tableFix0") String tableFix0,
                                            @Param("tableFix1") String tableFix1,
                                            @Param("startTime") String startTime,
                                            @Param("endTime") String endTime,
                                            @Param("numberType") Integer numberType);

    /**
     * 【供应链-单品】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportCommodity(@Param("tableFix0") String tableFix0,
                                            @Param("tableFix1") String tableFix1,
                                            @Param("startTime") String startTime,
                                            @Param("endTime") String endTime,
                                            @Param("numberType") Integer numberType);

    /**
     * 【点餐-门店销售】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportStoreSales(@Param("tableFix0") String tableFix0,
                                             @Param("tableFix1") String tableFix1,
                                             @Param("startTime") String startTime,
                                             @Param("endTime") String endTime,
                                             @Param("numberType") Integer numberType);

    /**
     * 【订单分析】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportStastics(@Param("tableFix0") String tableFix0,
                                           @Param("tableFix1") String tableFix1,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime,
                                           @Param("numberType") Integer numberType);

    /**
     * 【小程序门店使用数】
     *
     * @param tableFix0
     * @param tableFix1
     * @param startTime
     * @param endTime
     * @return
     */
    Integer getAppSalesNum(@Param("tableFix0") String tableFix0,
                           @Param("tableFix1") String tableFix1,
                           @Param("startTime") String startTime,
                           @Param("endTime") String endTime);

    /**
     * 采购占比
     *
     * @param tableFix0
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderReportDTO> getReportBuyProportion(@Param("tableFix0") String tableFix0,
                                                @Param("startTime") String startTime,
                                                @Param("endTime") String endTime,
                                                @Param("numberType") Integer numberType);

    /**
     * 门店列表
     *
     * @return
     */
    List<ReportSelectRespVO> getReportStoreList();

    /**
     * 仓库列表
     *
     * @return
     */
    List<ReportSelectRespVO> getReportWarehouseList();

}