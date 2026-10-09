package com.htyoudao.youdao.framework.common.exception.enums;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 * 全局错误码枚举
 * 0-999 系统异常编码保留
 *
 * 一般情况下，使用 HTTP 响应状态码 https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status
 * 虽然说，HTTP 响应状态码作为业务使用表达能力偏弱，但是使用在系统层面还是非常不错的
 * 比较特殊的是，因为之前一直使用 0 作为成功，就不使用 200 啦。
 *
 * @author 0090
 */
public interface GlobalErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(0, "成功");

    // ========== 客户端错误段 ==========

    ErrorCode BAD_REQUEST = new ErrorCode(400, "请求参数不正确");
    ErrorCode UNAUTHORIZED = new ErrorCode(401, "账号未登录");
    ErrorCode FORBIDDEN = new ErrorCode(403, "没有该操作权限");
    ErrorCode NOT_FOUND = new ErrorCode(404, "请求未找到");
    ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(405, "请求方法不正确");
    ErrorCode LOCKED = new ErrorCode(423, "请求失败，请稍后重试"); // 并发请求，不允许
    ErrorCode TOO_MANY_REQUESTS = new ErrorCode(429, "服务压力过大，请稍后重试!");

    // ========== 服务端错误段 ==========

    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, "系统繁忙，请稍后重试");
    ErrorCode NOT_IMPLEMENTED = new ErrorCode(501, "系统服务调整，当前部分功能暂时无法使用，请稍后再试");
    ErrorCode ERROR_CONFIGURATION = new ErrorCode(502, "错误的配置项");

    // ========== 自定义错误段 ==========
    ErrorCode REPEATED_REQUESTS = new ErrorCode(900, "重复请求，请稍后重试"); // 重复请求
    ErrorCode DEMO_DENY = new ErrorCode(901, "演示模式，禁止写操作");
    ErrorCode DUPLICATE_RECORD = new ErrorCode(902, "存在重复记录");
    ErrorCode RPC_EXCEPTION = new ErrorCode(903, "网络不稳定，请稍后尝试");
    ErrorCode SIGN_EXCEPTION = new ErrorCode(904, "签名认证失败");
    ErrorCode REQUEST_TIMEOUT = new ErrorCode(905, "请求已过期");
    ErrorCode REQUEST_LESS = new ErrorCode(906, "请求信息缺失");

    ErrorCode UNKNOWN = new ErrorCode(999, "未知错误");

    // ========== excel相关 ==========
    ErrorCode EXCEL_RSP_CAN_NOT_BE_NULL = new ErrorCode(600, "HttpServletResponse不能为空");
    ErrorCode EXCEL_FILENAME_CAN_NOT_BE_EMPTY = new ErrorCode(601, "文件名不能为空");
    ErrorCode EXCEL_CLASS_CAN_NOT_BE_NULL = new ErrorCode(602, "Class元类不能为空");
    ErrorCode EXCEL_DATA_CAN_NOT_BE_EMPTY = new ErrorCode(603, "导出的内容不能为空");
    ErrorCode EXCEL_EXPORT_FILE_FAILED = new ErrorCode(604, "导出文件 [%s] 失败: %s");
    ErrorCode EXCEL_RESOURCE_CLOSE_FAILED = new ErrorCode(605, "资源关闭失败");
    ErrorCode EXCEL_MULTIPART_UPLOAD_FAILD = new ErrorCode(606, "分片上传文件失败");

    ErrorCode EXCEL_IMPORT_FILE_FAILED = new ErrorCode(800, "导入文件失败");


}
