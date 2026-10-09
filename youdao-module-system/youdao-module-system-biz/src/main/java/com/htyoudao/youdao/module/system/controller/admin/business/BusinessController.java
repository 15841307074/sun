package com.htyoudao.youdao.module.system.controller.admin.business;

import com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.htyoudao.youdao.module.system.api.oauth2.OAuth2TokenApi;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.UserRespVO;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.module.system.controller.admin.business.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.business.BusinessDO;
import com.htyoudao.youdao.module.system.service.business.BusinessService;

@Tag(name = "管理后台 - 项目")
@RestController
@RequestMapping("/system/business")
@Validated
public class BusinessController {

    @Resource
    private BusinessService businessService;

    @Resource
    private AdminUserService userService;

    @Resource
    private OAuth2TokenApi oAuth2TokenApi;

    private static final String HEAD_NAME = "Authorization";

    private static final String TOKEN_PARAMETER = "token";

    @PostMapping("/create")
    @Operation(summary = "创建项目")
    @PreAuthorize("@ss.hasPermission('system:business:create')")
    public CommonResult<Long> createBusiness(@Valid @RequestBody BusinessSaveReqVO createReqVO) {
        return success(businessService.createBusiness(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目")
    @PreAuthorize("@ss.hasPermission('system:business:update')")
    public CommonResult<Boolean> updateBusiness(@Valid @RequestBody BusinessSaveReqVO updateReqVO) {
        businessService.updateBusiness(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:business:delete')")
    public CommonResult<Boolean> deleteBusiness(@RequestParam("id") Long id) {
        businessService.deleteBusiness(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:business:query')")
    public CommonResult<BusinessRespVO> getBusiness(@RequestParam("id") Long id) {
        BusinessDO business = businessService.getBusiness(id);
        BusinessRespVO respVO = BeanUtils.toBean(business, BusinessRespVO.class);
        if (StringUtils.isNotBlank(business.getMenuIds())){
            respVO.setMenuIds(Arrays.stream(business.getMenuIds().split(",")).toList());
        }
        if (respVO.getUserId() != null) {
            UserRespVO userDetail = userService.getUserDetail(respVO.getUserId());
            if (userDetail != null) {
                respVO.setUsername(userDetail.getUsername());
                respVO.setNickname(userDetail.getUserNickname());
                respVO.setMobile(userDetail.getMobile());
            }
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得项目分页")
    @PreAuthorize("@ss.hasPermission('system:business:query')")
    public CommonResult<PageResult<BusinessPageRespVO>> getBusinessPage(@Valid BusinessPageReqVO pageReqVO) {
        return success(businessService.getBusinessPage(pageReqVO));
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改项目状态")
    @PreAuthorize("@ss.hasPermission('system:business:update')")
    public CommonResult<Boolean> updateBusinessStatus(@Valid @RequestBody BusinessUpdateStatusReqVO reqVO) {
        businessService.updateBusinessStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }


    @GetMapping("/page/user")
    @Operation(summary = "获得用户选择项目分页")
    public CommonResult<List<BusinessUserRespVO>> getUserList(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request, HEAD_NAME, TOKEN_PARAMETER);
        if (oAuth2TokenApi.checkAccessToken(token) == null) {
            throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "token已过期");
        }
        List<BusinessUserRespVO> businessPageByUser = businessService.getUserList(
            SecurityFrameworkUtils.getLoginUserId());

        return success(businessPageByUser);
    }


    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "项目列表", description = "只包含被开启的项目列表，主要用于前端的下拉选项")
    public CommonResult<List<BusinessDO>> getBusinessList() {
        return success(businessService.getBusinessList());
    }

    @GetMapping("/listAll")
    @Operation(summary = "项目列表", description = "远程调用")
    public CommonResult<List<BusinessDTO>> listAll() {
        List<BusinessDTO> list = businessService.listAll();
        return success(list);
    }


    @GetMapping("/selectByName")
    @Operation(summary = "获取项目名", description = "远程调用")
    public CommonResult<String> selectByName(@RequestParam("id") Long id) {
        String businessName = businessService.getBusinessName(id);
        return success(businessName);
    }

    @GetMapping("/listAllStore")
    @Operation(summary = "项目门店列表", description = "远程调用")
    @DataPermission(enable = false)
    public CommonResult<List<BusinessStoreRespVO>> listAllStore() {
        List<BusinessStoreRespVO> list = businessService.listAllStore();
        return success(list);
    }
}