package com.htyoudao.youdao.framework.sentinel.core.handler;


import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.servlet.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Sentinel 自定义阻断异常处理
 *
 * @author liuzhaowang
 */
@Slf4j
public class SentinelExceptionHandler implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, String s, BlockException e) {
        String msg = "未知异常";
        int status = HttpStatus.TOO_MANY_REQUESTS.value();
        if (e instanceof FlowException) {
            msg = "前方拥堵，请稍后再试~";
        } else if (e instanceof ParamFlowException) {
            msg = "活动前方拥堵，请稍后再试~";
        } else if (e instanceof DegradeException) {
            msg = "系统前方拥堵，请稍后再试~";
        } else if (e instanceof AuthorityException) {
            msg = "暂无权限访问，请稍后再试~";
            status = HttpStatus.UNAUTHORIZED.value();
        }
        if (e instanceof SystemBlockException sys) {
            msg = "网络波动，请稍后再试~";
            log.error("[SystemBlockException][ URI={}, Type={}]", sys.getResourceName(), sys.getLimitType());
        } else {
            // 一行打印：URL + 规则类型 + 规则名 + 异常堆栈
            log.error("[SentinelBlocked][ URI={}, Rule={}, Cause={} ]",
                    request.getRequestURI(),           // ① 原始 URI
                    e.getRule(),                       // ② 触发规则对象（含规则名）
                    e.getClass().getSimpleName());      // ③ 规则类型（Flow/Degrade…）
        }
        ServletUtils.writeJSON(response, CommonResult.error(status, msg));
    }
}
