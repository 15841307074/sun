package com.htyoudao.youdao.module.member.controller.admin.wecom;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckAnyMemberReqVO;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckMemberReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wecom.WecomApiUtil;
import com.htyoudao.youdao.module.member.service.wecom.WecomGroupService;
import com.htyoudao.youdao.module.member.service.wecom.WecomGroupVersionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import java.io.IOException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "企业微信 - 社群管理")
@RestController
@RequestMapping("/member/wecom/group")
@Validated
@Slf4j
public class WecomGroupController {

    @Resource
    @Qualifier("wecomGroupDatabaseService")
    private WecomGroupService wecomGroupService;

    @Resource
    private WecomApiUtil wecomApiUtil;

    @Resource
    private WecomGroupVersionUtil wecomGroupVersionUtil;


    /**
     * 判断用户是否在企业微信社群中
     */
    @PostMapping("/check-member")
    @Operation(summary = "判断用户是否在企业微信社群中")
    public CommonResult<Boolean> checkMember(@RequestBody @Validated WecomGroupCheckMemberReqVO reqVO) {
        return success(wecomGroupService.isUserInGroup(reqVO.getUnionId(), reqVO.getChatId()));
    }


    /**
     * 判断用户是否在任意一个企业微信社群中
     */
    @PostMapping("/check-any-group")
    @Operation(summary = "判断用户是否在任意一个企业微信社群中")
    public CommonResult<Boolean> checkAnyGroup(@RequestBody @Validated WecomGroupCheckAnyMemberReqVO reqVO) {
        return success(wecomGroupService.isUserInAnyGroup(reqVO.getUnionId()));
    }


    /**
     * 初始化所有群聊版本（一次性任务）
     */
    @PostMapping("/initAllVersion")
    @Operation(summary = "初始化所有群聊版本")
    @PermitAll
    public void initAllGroupVersion() throws IOException {
        log.info("[WeCom群初始化] 开始执行所有群聊版本初始化");

        Set<String> chatIdSet = wecomApiUtil.getGroupList();
        if (chatIdSet == null || chatIdSet.isEmpty()) {
            log.warn("[WeCom群初始化] 群聊列表为空，结束");
            return;
        }

        int total = chatIdSet.size();
        int success = 0;
        int fail = 0;

        for (String chatId : chatIdSet) {
            try {
                String version = wecomGroupService.handleUnreliableEvent(chatId);
                wecomGroupVersionUtil.updateVersion(chatId, version);

                success++;
                log.info("[WeCom群初始化] chatId={} 初始化成功, version={}", chatId, version);
            } catch (Exception e) {
                fail++;
                log.error("[WeCom群初始化] chatId={} 初始化失败", chatId, e);
            }
        }

        log.info("[WeCom群初始化] 完成，群聊总数={}, 成功={}, 失败={}", total, success, fail);
    }
}

