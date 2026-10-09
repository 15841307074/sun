package com.htyoudao.youdao.module.member.api.crowd;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.member.api.crowd.dto.CrowdNameDTO;
import com.htyoudao.youdao.module.member.api.crowd.vo.CustomCrowdRespVO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberCrowdDTO;
import com.htyoudao.youdao.module.member.service.crowd.CustomCrowdService;
import com.htyoudao.youdao.module.member.service.crowd.WxMemberCrowdRefService;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * @author dht
 */
@DubboService(timeout = 50000)
@Slf4j
public class CrowdApiImpl implements CrowdApi{

    @Resource
    private WxMemberCrowdRefService wxMemberCrowdRefService;

    @Resource
    private CustomCrowdService crowdService;

    @Resource
    private WxMemberService wxMemberService;


    @Override
    public Boolean memberExist(Long memberId, String crowdId) {
        return wxMemberCrowdRefService.memberExist(memberId,crowdId);
    }

    @Override
    public List<WxMemberCrowdDTO> getMemberDataByCrowdId(String crowdId) {
        return wxMemberService.getMemberDataByCrowdId(crowdId);
    }

    @Override
    public List<CrowdNameDTO> getByIds(List<Long> allCrowd) {
        return crowdService.getByIds(allCrowd);
    }




    @Override
    public CommonResult<CustomCrowdRespVO> getById(Long id) {
        com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo.CustomCrowdRespVO byId = crowdService.getById(id);
        if(byId == null){
            return CommonResult.success(null);
        }
        com.htyoudao.youdao.module.member.api.crowd.vo.CustomCrowdRespVO customCrowdRespVO = new CustomCrowdRespVO();
        BeanUtils.copyProperties(byId,customCrowdRespVO);
        return CommonResult.success(customCrowdRespVO);
    }


}
