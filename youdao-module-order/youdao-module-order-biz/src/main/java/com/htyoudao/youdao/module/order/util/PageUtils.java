package com.htyoudao.youdao.module.order.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.dal.dataobject.page.PageDomain;
import com.htyoudao.youdao.module.order.dal.dataobject.page.TableSupport;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageUtils {
    /**
     * 分页对象
     */
    public static <T> Page<T> getPageInfo() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        return new Page<>(pageNum, pageSize);
    }
    public static Pageable getESPageable() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return pageable;
    }

    public static <T> QueryWrapper<T> buildPageQueryWrapper(QueryWrapper<T> queryWrapper, Page<T> page) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        Integer begin = (pageNum - 1) * pageSize;
        Integer end = pageNum * pageSize;
        queryWrapper.last("limit " + begin + "," + end);
        page.setSize(pageSize);
        return queryWrapper;
    }

    public static <T> Page<T>  subListPage(List<T> list) {
        Page<T> page = new Page<>();
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        Integer begin = (pageNum - 1) * pageSize;
        Integer end = pageNum * pageSize;
        if (begin > list.size()) {
            begin = list.size();
        }
        if (end > list.size()) {
            end = list.size();
        }
        page.setTotal(list.size());
        page.setRecords(list.subList(begin, end));
        return page;
    }
//    /**
//     * 返回分页响应信息
//     *
//     * @param page
//     * @return
//     */
//    public static CommonResult getTableDataInfo(Page page) {
//        CommonResult ajaxResult = CommonResult.success("查询成功");
//        ajaxResult.put("rows", page.getRecords());
//        ajaxResult.put("total", page.getTotal());
//        return ajaxResult;
//    }
//
//    public static AjaxResult getTableDataInfo(List list, long total) {
//        AjaxResult ajaxResult = AjaxResult.success("查询成功");
//        ajaxResult.put("rows", list);
//        ajaxResult.put("total", total);
//        return ajaxResult;
//    }
}
