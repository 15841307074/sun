package com.htyoudao.youdao.module.system.controller.app.business;

import com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.system.api.oauth2.OAuth2TokenApi;
import jakarta.annotation.security.PermitAll;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.module.system.controller.admin.business.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.business.BusinessDO;
import com.htyoudao.youdao.module.system.service.business.BusinessService;

@Tag(name = "app - 项目")
@RestController
@RequestMapping("/system/business/dc")
@Validated
public class BusinessAppController {

    @Resource
    private BusinessService businessService;
    @Resource
    private OAuth2TokenApi oAuth2TokenApi;

    private static final String HEAD_NAME = "Authorization";

    private static final String TOKEN_PARAMETER = "token";


    @GetMapping("/get")
    @Operation(summary = "获得项目")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<BusinessRespVO> getBusinessById(@RequestParam("id") Long id) {
        BusinessDO business = businessService.getBusiness(id);
        if (business == null){
            return success(null);
        }
        BusinessRespVO respVO = BeanUtils.toBean(business, BusinessRespVO.class);
        if (StringUtils.isNotBlank(business.getMenuIds())){
            respVO.setMenuIds(Arrays.stream(business.getMenuIds().split(",")).toList());
        }
        return success(respVO);
    }
    @GetMapping("/getBusinessListByUser")
    @Operation(summary = "根据登入人获得项目列表")
    public CommonResult<List<BusinessUserRespVO>> getBusinessListByUser(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request, HEAD_NAME, TOKEN_PARAMETER);
        if (oAuth2TokenApi.checkAccessToken(token) == null) {
            throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "token已过期");
        }
        List<BusinessUserRespVO> businessPageByUser = businessService.getUserByBossList(
                SecurityFrameworkUtils.getLoginUserId());

        return success(businessPageByUser);
    }


}