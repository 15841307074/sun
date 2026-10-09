package com.htyoudao.youdao.module.promotion.controller.admin.usercoupon;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponExpiredArchiveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponExpiredArchiveRespVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.archive.UserCouponExpiredArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 用户券历史数据归档。
 */
@Tag(name = "管理后台 - 用户券历史数据归档")
@RestController
@RequestMapping("/promotion/user-coupon/archive")
public class UserCouponArchiveController {

    private final UserCouponExpiredArchiveService archiveService;

    public UserCouponArchiveController(UserCouponExpiredArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @PostMapping("/expired")
    @Operation(summary = "按过期时间分批归档用户券",
            description = "固定小批次独立事务；不同分表有限并发，同一分表串行处理")
    @PreAuthorize("@ss.hasPermission('promotion:user-coupon:archive')")
    @DataPermission(enable = false)
    public CommonResult<UserCouponExpiredArchiveRespVO> archiveExpired(
            @Valid @RequestBody UserCouponExpiredArchiveReqVO reqVO) {
        return success(archiveService.archive(reqVO));
    }
}
