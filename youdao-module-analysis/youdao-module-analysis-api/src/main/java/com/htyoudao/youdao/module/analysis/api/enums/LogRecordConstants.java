package com.htyoudao.youdao.module.analysis.api.enums;

/**
 * Promotion 操作日志枚举
 * 目的：统一管理，也减少 Service 里各种“复杂”字符串
 *
 * @author 0090
 */
public interface LogRecordConstants {


    // ======================= 报表下载 =======================
    String ANALYSIS_REPORT_DOWNLOAD_TYPE = "报表下载";

    String ANALYSIS_REPORT_ORDER_DOWNLOAD_SUB_TYPE = "导出订单报表";
    String ANALYSIS_REPORT_ORDER_DOWNLOAD_SUCCESS = "导出了订单报表，入参【{{#requestVO}}】";

    String ANALYSIS_REPORT_STORE_DOWNLOAD_SUB_TYPE = "导出门店报表";
    String ANALYSIS_REPORT_STORE_DOWNLOAD_SUCCESS = "导出了门店报表，入参【{{#requestVO}}】";

    String ANALYSIS_REPORT_MARKETING_STORE_DOWNLOAD_SUB_TYPE = "导出营销分析门店报表";
    String ANALYSIS_REPORT_MARKETING_STORE_DOWNLOAD_SUCCESS = "导出了营销分析门店报表，入参【{{#requestVO}}】";

    String ANALYSIS_REPORT_MARKETING_CHANNEL_DOWNLOAD_SUB_TYPE = "导出营销分析渠道报表";
    String ANALYSIS_REPORT_MARKETING_CHANNEL_DOWNLOAD_SUCCESS = "导出了营销分析渠道报表，入参【{{#requestVO}}】";

    String ANALYSIS_REPORT_PRODUCT_DOWNLOAD_SUB_TYPE = "导出商品报表";
    String ANALYSIS_REPORT_PRODUCT_DOWNLOAD_SUCCESS = "导出了商品报表，入参【{{#requestVO}}】";

    String ANALYSIS_REPORT_PRODUCT_ALL_DOWNLOAD_SUB_TYPE = "导出商品报表All";
    String ANALYSIS_REPORT_PRODUCT_ALL_DOWNLOAD_SUCCESS = "导出了商品报表All，入参【{{#requestVO}}】";

    // ======================= 营销活动 =======================
    String PROMOTION_ACTIVITYNJNZ_TYPE ="活动";
    String PROMOTION_ACTIVITYNJNZ_EXPORT_TYPE= "导出了活动数据";
    String PROMOTION_ACTIVITYNJNZ_EXPORT_SUCCESS = "导出了活动数据【{{#activityNjnz.activityId}}】";

    String PROMOTION_ACTIVITYNJNZ_STORE_EXPORT_TYPE= "导出了门店活动明细";
    String PROMOTION_ACTIVITYNJNZ_STORE_EXPORT_SUCCESS = "导出了【{{#activityNjnz.storeId}}】门店活动明细";
}
