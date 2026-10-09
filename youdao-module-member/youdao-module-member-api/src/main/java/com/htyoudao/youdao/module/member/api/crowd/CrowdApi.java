package com.htyoudao.youdao.module.member.api.crowd;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.crowd.vo.CustomCrowdRespVO;
import com.htyoudao.youdao.module.member.api.crowd.dto.CrowdNameDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberCrowdDTO;

import java.util.List;

/**
 * @author dht
 */
@Tag(name = "人群")
public interface CrowdApi {

    /**
     * 判断会员是否属于人群
     * @param memberId memberId
     * @param crowdId crowdId
     * @return Boolean
     */
    Boolean memberExist(Long memberId,String crowdId);



    CommonResult<CustomCrowdRespVO> getById(@RequestParam("id") Long id);

    /**
     * 营销短信通过人群查人
     * @param crowdId crowdId
     * @return Boolean
     */
    List<WxMemberCrowdDTO> getMemberDataByCrowdId(String crowdId);

    /**
     * 根据ids查询
     * @param allCrowd allCrowd
     * @return Boolean
     */
    List<CrowdNameDTO> getByIds(List<Long> allCrowd);
}
