package com.htyoudao.youdao.module.system.util.page;


/**
 * 分页数据
 * 
 *
 */
public class PageDomain
{
    /** 当前记录起始索引 */
    private Integer pageNo;

    /** 每页显示记录数 */
    private Integer pageSize;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
    }

}
