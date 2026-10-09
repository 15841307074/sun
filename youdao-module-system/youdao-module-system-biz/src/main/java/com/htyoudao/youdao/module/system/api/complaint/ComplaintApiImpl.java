package com.htyoudao.youdao.module.system.api.complaint;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.service.complant.ComplaintAppServiceImpl;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class ComplaintApiImpl implements ComplaintApi {
   @Resource
   private ComplaintAppServiceImpl complaintService;

    @Override
    public CommonResult<List<String>> getComplaintListByMemberId(Long memberId) {
        return success(complaintService.getComplaintListByMemberId(memberId));
    };
    @Override
    public  CommonResult<Map<Integer, Map<String, Object>>> getComplaintAggByStore(Long storeId) {
        return success(complaintService.getComplaintAggByStore(storeId));
    };
}
