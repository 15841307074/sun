package com.htyoudao.youdao.module.promotion.api.activitychannelname;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

/**
 * @author dht
 */
@Tag(name = "RPC 服务 - 渠道名称")
public interface ActivityChannelNameApi {

    CommonResult<Map<Long,String>> selectChannelNameMap();
}
