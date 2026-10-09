package com.htyoudao.youdao.framework.excel.core.pojo;

import co.elastic.clients.elasticsearch._types.FieldValue;
import java.util.List;

/**
 *
 * <p>
 * 
 * </p>
 *
 * @author zhangjihe
 *
 * @since 2025-10-28
 */
public interface SearchAfterSupport {
    void setSearchAfter(List<FieldValue> searchAfter);
    List<FieldValue> getSearchAfter();
}

