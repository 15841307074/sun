package com.htyoudao.youdao.module.member.controller.admin.crowd;

import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.member.api.wxmember.dto.MemberOrderDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberDataVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRespVO;
import com.htyoudao.youdao.module.member.service.crowd.WxMemberCrowdRefService;
import com.htyoudao.youdao.module.member.service.job.JobService;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 微信小程序用户管理Controller
 *
 * @author lbw
 * */


@Tag(name = "人群")
@RestController
@RequestMapping("/member/crowd")
@Validated
public class WxMemberCrowdController {

    @Resource
    private JobService jobService;





/*
    @GetMapping("/CreateCrowdRef")
    @Operation(summary = "测试人群会员关系创建")
    @PermitAll
    public void CreateCrowdRef() throws IOException {
        jobService.createMemberCrowdTask(10);
    }*/


}
