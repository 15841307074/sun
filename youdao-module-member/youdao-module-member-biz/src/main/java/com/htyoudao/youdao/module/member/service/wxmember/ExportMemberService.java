package com.htyoudao.youdao.module.member.service.wxmember;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberExcelRespVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberReqVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author dht
 */
public interface ExportMemberService {

    List<WxMemberExcelRespVO> getMemberExportList(Page<WxMemberExcelRespVO> param,
                                                  WxMemberReqVO wxMemberReqVO,
                                                  Map<Long, String> storeMap,
                                                  Map<Long, String> storeIdToOrgNameMap,
                                                  Map<Long, String> memberTagMap,
                                                  Map<Long, BigDecimal> runnerBalanceMap);
}
