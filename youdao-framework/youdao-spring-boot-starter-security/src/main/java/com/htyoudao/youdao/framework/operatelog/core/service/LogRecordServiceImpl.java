package com.htyoudao.youdao.framework.operatelog.core.service;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.common.util.monitor.TracerUtils;
import com.htyoudao.youdao.framework.common.util.servlet.ServletUtils;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.system.api.logger.OperateLogApi;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;

import java.util.*;

/**
 * 操作日志 ILogRecordService 实现类
 * <p>
 * 基于 {@link OperateLogApi} 实现，记录操作日志
 *
 * @author HUIHUI
 */
@Slf4j
public class LogRecordServiceImpl implements ILogRecordService {

    @DubboReference
    private OperateLogApi operateLogApi;

    @Override
    public void record(LogRecord logRecord) {
        if (logRecord == null) {
            return;
        }

        try {
            // 创建并填充操作日志（包含扩展信息）
            OperateLogCreateReqDTO reqDTO = createOperateLog(logRecord);

            // 如果用户为空，没有必要记录操作日志
            if (reqDTO.getUserId() == null) {
                return;
            }

            // 异步记录日志
            operateLogApi.createOperateLogAsync(reqDTO);
        } catch (Throwable ex) {
            // 记录详细的异常信息，包括请求URL、操作类型、业务ID等关键信息
            String type = logRecord.getType();
            String subType = logRecord.getSubType();
            String bizNo = logRecord.getBizNo();
            String action = logRecord.getAction();

            log.error("[record][操作日志记录失败] 类型:{}, 子类型:{}, 业务ID:{}, 操作:{}, 异常原因:{}",
                    type, subType, bizNo, action, ex.getMessage(), ex);
        }
    }

    /**
     * 创建操作日志对象并填充数据
     *
     * @param logRecord 日志记录
     * @return 操作日志创建请求DTO
     */
    private OperateLogCreateReqDTO createOperateLog(LogRecord logRecord) {
        OperateLogCreateReqDTO reqDTO = new OperateLogCreateReqDTO();
        reqDTO.setTraceId(TracerUtils.getTraceId());

        // 填充用户信息
        fillUserFields(reqDTO);

        // 填充模块信息
        fillModuleFields(reqDTO, logRecord);

        // 填充请求信息和扩展信息
        fillRequestAndExtendFields(reqDTO, logRecord);

        return reqDTO;
    }

    /**
     * 填充用户字段
     *
     * @param reqDTO 操作日志请求DTO
     */
    private static void fillUserFields(OperateLogCreateReqDTO reqDTO) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser != null) {
            reqDTO.setUserId(loginUser.getId());
            reqDTO.setUserType(loginUser.getUserType());
            String name = SecurityFrameworkUtils.getLoginUserNickname();
            if (StrUtil.isEmpty(name)) {
                String username = SecurityFrameworkUtils.getLoginUsername();
                name = username == null ? "" : username;
            }
            reqDTO.setUsername(name);
        }
    }

    /**
     * 填充模块字段
     *
     * @param reqDTO    操作日志请求DTO
     * @param logRecord 日志记录
     */
    private static void fillModuleFields(OperateLogCreateReqDTO reqDTO, LogRecord logRecord) {
        reqDTO.setType(logRecord.getType());
        reqDTO.setSubType(logRecord.getSubType());
        String bizNo = logRecord.getBizNo();
        reqDTO.setBizId(Validator.isNumber(bizNo) ? Long.parseLong(bizNo) : 0L);
        reqDTO.setAction(StrUtil.maxLength(logRecord.getAction(), 2000));
        reqDTO.setExtra(StrUtil.maxLength(logRecord.getExtra(), 2000));
    }

    /**
     * 填充请求字段和扩展字段
     * 合并了请求基本信息和扩展信息的处理逻辑，统一处理HTTP请求相关数据
     *
     * @param reqDTO    操作日志请求DTO
     * @param logRecord 日志记录
     */
    private void fillRequestAndExtendFields(OperateLogCreateReqDTO reqDTO, LogRecord logRecord) {
        // 获取请求对象，如果没有则不处理
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return;
        }

        try {
            // 1. 填充基本请求信息
            reqDTO.setRequestMethod(request.getMethod());
            reqDTO.setRequestUrl(request.getRequestURI());
            reqDTO.setUserIp(ServletUtils.getClientIP(request));
            reqDTO.setUserAgent(ServletUtils.getUserAgent(request));
            if (logRecord.getExtra() != null) {
                reqDTO.setExtra(logRecord.getExtra());
            }

            // 2. 填充请求参数（包括URL参数和请求体）
            Map<String, Object> requestParams = collectRequestParams(request);
            if (!requestParams.isEmpty()) {
                reqDTO.setRequestParams(JsonUtils.toJsonString(requestParams));
            }

            // 3. 填充请求头
            Map<String, String> headers = getRequestHeaders(request);
            if (!headers.isEmpty()) {
                reqDTO.setRequestHeaders(JsonUtils.toJsonString(headers));
            }

        } catch (Throwable ex) {
            // 记录详细的异常信息，但不影响主流程
            log.warn("[fillRequestAndExtendFields] 填充请求和扩展字段失败: {}", ex.getMessage());
        }
    }

    /**
     * 收集请求参数，整合URL参数和请求体
     *
     * @param request HTTP请求
     * @return 请求参数Map
     */
    private Map<String, Object> collectRequestParams(HttpServletRequest request) {
        // 获取URL参数
        Map<String, String> queryParams = ServletUtils.getParamMap(request);

        // 获取请求体 - 只处理JSON请求，直接获取原始字符串
        String requestBody = ServletUtils.isJsonRequest(request) ? ServletUtils.getBody(request) : null;

        // 如果都为空，返回空Map
        if (queryParams.isEmpty() && StrUtil.isEmpty(requestBody)) {
            return Collections.emptyMap();
        }

        return MapUtil.<String, Object>builder()
                .put("query", queryParams.isEmpty() ? null : queryParams)
                .put("body", requestBody).build();
    }

    /**
     * 获取请求头信息
     *
     * @param request HTTP请求
     * @return 请求头Map
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        // 使用Enumeration遍历请求头，避免创建不必要的集合
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null || !headerNames.hasMoreElements()) {
            return Collections.emptyMap();
        }

        Map<String, String> headers = new HashMap<>(16);
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    @Override
    public List<LogRecord> queryLog(String bizNo, String type) {
        throw new UnsupportedOperationException("使用 OperateLogApi 进行操作日志的查询");
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        throw new UnsupportedOperationException("使用 OperateLogApi 进行操作日志的查询");
    }
}
