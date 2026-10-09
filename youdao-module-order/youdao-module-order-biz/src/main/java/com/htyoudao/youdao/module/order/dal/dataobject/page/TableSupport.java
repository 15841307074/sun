package com.htyoudao.youdao.module.order.dal.dataobject.page;

import cn.hutool.core.convert.Convert;
import com.htyoudao.youdao.module.order.util.ServletUtils;


/**
 * 表格数据处理
 *
 *
 */
public class TableSupport {
    /**
     * 当前记录起始索引
     */
    private static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    private static final String PAGE_SIZE = "pageSize";

    /**
     * 封装分页对象
     */
    private static PageDomain getPageDomain() {
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(Convert.toInt(ServletUtils.getParameter(PAGE_NUM), 1));
        pageDomain.setPageSize(Convert.toInt(ServletUtils.getParameter(PAGE_SIZE), 10));
        return pageDomain;
    }

    public static PageDomain buildPageRequest() {
        return getPageDomain();
    }
}
