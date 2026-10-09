package com.htyoudao.youdao.module.member.controller.admin.wxmember.vo;


import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 会员等级分页返回对象
 */
@Data
public class MemberLevelPageResult {
    /**
     * 总记录数
     */
    private long total;
    /**
     * 总页数
     */
    private long pages;
    /**
     * 当前页
     */
    private int current;
    /**
     * 每页条数
     */
    private int size;
    /**
     * 当前页的等级->会员DTO映射
     */
    private Map<Integer, List<WxMemberDTO>> levelMemberMap;
}
