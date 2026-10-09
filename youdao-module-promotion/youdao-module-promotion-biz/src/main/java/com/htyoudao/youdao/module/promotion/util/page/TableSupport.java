package com.htyoudao.youdao.module.promotion.util.page;

import com.htyoudao.youdao.module.promotion.util.servlet.ServletUtils;

import cn.hutool.core.convert.Convert;

/**
 * 表格数据处理
 *
 *
 */
public class TableSupport {
    /**
     * 当前记录起始索引
     */
    private static final String PAGE_NO = "pageNum";

    /**
     * 每页显示记录数
     */
    private static final String PAGE_SIZE = "pageSize";

    /**
     * 封装分页对象
     */
    private static PageDomain getPageDomain() {
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(Convert.toInt(ServletUtils.getParameter(PAGE_NO), 1));
        pageDomain.setPageSize(Convert.toInt(ServletUtils.getParameter(PAGE_SIZE), 10));
        return pageDomain;
    }

    public static PageDomain buildPageRequest() {
        return getPageDomain();
    }
}
