package com.htyoudao.youdao.module.order.dal.dataobject.page;


/**
 * 分页数据
 * 
 *
 */
public class PageDomain
{
    /** 当前记录起始索引 */
    private Integer pageNum;

    /** 每页显示记录数 */
    private Integer pageSize;

    public Integer getPageNum()
    {
        return pageNum;
    }

    void setPageNum(Integer pageNum)
    {
        this.pageNum = pageNum;
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
