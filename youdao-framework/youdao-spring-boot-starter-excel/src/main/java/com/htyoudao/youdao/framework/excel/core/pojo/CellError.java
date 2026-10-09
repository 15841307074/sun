package com.htyoudao.youdao.framework.excel.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * <p>
 * 
 * </p>
 *
 * @author zhangjihe
 *
 * @since 2025-04-07
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CellError {
    private Integer rowIndex;
    private Integer colIndex;
    private String message;

}
