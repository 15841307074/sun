package com.htyoudao.youdao.module.member.api.wecom;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckAnyMemberReqVO;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckMemberReqVO;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * 企业微信群 API
 */
@FeignClient(name = "member-server") // TODO 0090：fallbackFactory =
@Tag(name = "企业微信 - 社群管理")
public interface WecomGroupApi {

    /**
     * 判断用户是否在企业微信社群中
     *
     * @return 是否在群中
     */
    CommonResult<Boolean> isUserInGroup(WecomGroupCheckMemberReqVO reqVO);

    /**
     * 判断用户是否在任意一个企业微信社群中
     *
     * @return 是否在任意群中
     */
    CommonResult<Boolean> isUserInAnyGroup(WecomGroupCheckAnyMemberReqVO reqVO);
}
