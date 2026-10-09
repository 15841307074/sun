package com.htyoudao.youdao.module.system.api.tag;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "RPC 服务 - 门店标签")
public interface TagApi {

	/**
	 * 根据标签值 ids 批量获取标签名称
	 * @param ids 标签值 id 列表
	 * @return id -> name 映射
	 */
	Map<Long, String> getNamesByIds(List<Long> ids);

}
