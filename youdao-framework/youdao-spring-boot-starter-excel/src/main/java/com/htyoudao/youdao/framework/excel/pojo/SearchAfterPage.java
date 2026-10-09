package com.htyoudao.youdao.framework.excel.pojo;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-10-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchAfterPage<T> extends Page<T> {
    @Serial
    private static final long serialVersionUID = 3519391063967237745L;
    /**
     * 下一页的 search_after 游标
     */
    private List<FieldValue> searchAfter;

    @Schema(description = "数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<T> list;

    @Schema(description = "总量", requiredMode = Schema.RequiredMode.REQUIRED)
    private long total;

    public SearchAfterPage() {
    }

    public SearchAfterPage(List<T> list, Long total) {
        this.list = list;
        this.total = total;
    }

    public SearchAfterPage(Long total) {
        this.list = new ArrayList<>();
        this.total = total;
    }

    public static <T> SearchAfterPage<T> empty() {
        return new SearchAfterPage<>(0L);
    }

    public static <T> SearchAfterPage<T> empty(Long total) {
        return new SearchAfterPage<>(total);
    }
}

