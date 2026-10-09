package com.htyoudao.youdao.module.member.api.wecom;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckAnyMemberReqVO;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckMemberReqVO;
import com.htyoudao.youdao.module.member.service.wecom.WecomGroupService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 企业微信群 API 实现
 */
@DubboService
public class WecomGroupApiImpl implements WecomGroupApi {

    @Resource
    @Qualifier("wecomGroupDatabaseService")
    private WecomGroupService wecomGroupService;

    /**
     * 判断用户是否在企业微信社群中
     *
     * @return 是否在群中
     */
    @Override
    public CommonResult<Boolean> isUserInGroup(WecomGroupCheckMemberReqVO reqVO) {
        return CommonResult.success(wecomGroupService.isUserInGroup(reqVO.getUnionId(), reqVO.getChatId()));
    }

    /**
     * 判断用户是否在任意一个企业微信社群中
     *
     * @return 是否在任意群中
     */
    @Override
    public CommonResult<Boolean> isUserInAnyGroup(WecomGroupCheckAnyMemberReqVO reqVO) {
        return CommonResult.success(wecomGroupService.isUserInAnyGroup(reqVO.getUnionId()));
    }
}
