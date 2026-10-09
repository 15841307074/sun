package com.htyoudao.youdao.module.order.dal.dataobject.page;

import lombok.Data;

import java.util.List;

/**
 * 功能：分页工具类
 */

@Data
public class PageBean<T> {
    private long pageSize;
    private long pageNum;
    private long totalRecords;
    private List<T> list;
}

