package com.htyoudao.youdao.module.analysis.enums;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 // 模块 analysis 错误码区间[1_008_000_000~1_009_000_000)
 */
public interface ErrorCodeConstants {

    // ========== 参数配置 1-001-000-000 ==========
    ErrorCode ES_QUERY_ERROR = new ErrorCode(1_008_000_000, "查询超时,请缩短查询范围后重试");
    // ClickHouse 链路查询异常统一错误码（对齐 ES_QUERY_ERROR 模式，编号顺延）
    ErrorCode CH_QUERY_ERROR = new ErrorCode(1_008_000_011, "查询超时,请缩短查询范围后重试");
    ErrorCode METRIC_NOT_FOUND = new ErrorCode(1_008_000_001, "当前指标不支持折线图查询");
    ErrorCode DATA_PERMISSION_NONE = new ErrorCode(1_008_000_002, "当前用户不能查看该数据");
    ErrorCode PRODUCT_ALL_STORE = new ErrorCode(1_008_000_003, "多门店 只能指定一个品");

    ErrorCode DATA_PERMISSION_IS_EMPTY = new ErrorCode(1_008_000_004, "当前用户无查看权限");
    ErrorCode STORE_IS_EMPTY = new ErrorCode(1_008_000_005, "门店ID不能为空");


    ErrorCode ES_DATE_ERROR = new ErrorCode(1_008_000_007, "请选择日期范围");

    ErrorCode ES_CHANGE_DATE_ERROR = new ErrorCode(1_008_000_008, "选择的日期范围不能超过2个月");

    ErrorCode DATA_NULL = new ErrorCode(1_008_000_009, "没用导出数据");

    // ========== 广告埋点 ==========
    ErrorCode AD_EVENT_TYPE_INVALID = new ErrorCode(1_008_000_010, "无效的广告事件类型");
    ErrorCode AD_ID_EMPTY = new ErrorCode(1_008_000_011, "广告ID不能为空");
    ErrorCode AD_POSITION_INVALID = new ErrorCode(1_008_000_012, "广告位序号必须在1-15之间");
    ErrorCode TIME_RANGE_EXCEED_LIMIT = new ErrorCode(1_008_000_010, "选择的时间范围不能超过 90 天");


    // ========== 月度门店组织快照 ==========
    ErrorCode STORE_ORG_MONTH_INVALID = new ErrorCode(1_008_000_020, "月份格式错误，请使用 yyyy-MM");
    ErrorCode STORE_ORG_PERMISSION_DENIED = new ErrorCode(1_008_000_021, "当前用户无权查看该组织");
    ErrorCode STORE_ORG_SNAPSHOT_NOT_FOUND = new ErrorCode(1_008_000_022, "指定月份的门店组织快照尚未生成");
    ErrorCode STORE_ORG_SNAPSHOT_DATA_INVALID = new ErrorCode(1_008_000_023, "门店组织快照数据异常");

    // ========== 投诉异常排行 ==========
    ErrorCode COMPLAINT_RANKING_DATE_REQUIRED = new ErrorCode(1_008_000_024, "开始日期和结束日期不能为空");
    ErrorCode COMPLAINT_RANKING_DATE_INVALID = new ErrorCode(1_008_000_025, "开始日期不能晚于结束日期，且日期范围不能超过 90 天");
    ErrorCode COMPLAINT_RANKING_TYPE_INVALID = new ErrorCode(1_008_000_026, "排行类型仅支持 TOP 或 BOTTOM");

}
