package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil;
/** 抽奖新增配置错误统一以业务提示返回。 */
public final class LotteryErrors {
    private LotteryErrors(){}
    public static ServiceException invalid(String message){return ServiceExceptionUtil.exception(new ErrorCode(1_006_005_036,"抽奖配置异常：{}"),message);}
}
