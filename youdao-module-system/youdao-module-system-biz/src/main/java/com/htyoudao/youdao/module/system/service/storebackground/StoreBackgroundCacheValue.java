package com.htyoudao.youdao.module.system.service.storebackground;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门店背景 Redis 缓存值。
 *
 * 使用明确类型转换为 JSON 字符串，避免使用 Object 类型序列化。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreBackgroundCacheValue {

    /** 最终命中的模板编号。 */
    private Long templateId;
    /** 最终展示的背景图片地址。 */
    private String imageUrl;
    /** 模板最后发布时间。 */
    private LocalDateTime releaseTime;
    /** 是否命中了系统默认模板。 */
    private Boolean defaultTemplate;
}
