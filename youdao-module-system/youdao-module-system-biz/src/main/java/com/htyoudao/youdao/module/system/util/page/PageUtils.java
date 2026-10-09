package com.htyoudao.youdao.module.system.util.page;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageUtils {
    /**
     * 分页对象
     */
    public static <T> Page<T> getPageInfo() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNo();
        Integer pageSize = pageDomain.getPageSize();
        return new Page<>(pageNum, pageSize);
    }

}
