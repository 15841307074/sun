package com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare;

import com.alibaba.excel.EasyExcel;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;

import com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO.CouponPackageSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO.CouponPackageDetailReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO.CouponPackageSendAO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO.CouponPackageShareRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.MemberExlVo;
import com.htyoudao.youdao.module.promotion.service.couponPackageShare.CouponPackageShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "管理后台 - 优惠券包推广")
@RestController
@RequestMapping("/promotion/coupon-package-share")
@Validated
public class CouponPackageShareController {

    @Resource
    private CouponPackageShareService couponPackageShareService;

    @PostMapping("/create")
    @Operation(summary = "创建或修改优惠券包推广")
    //@PreAuthorize("@ss.hasPermission('package:share:create')")
    public CommonResult<Boolean> createCouponPackageShare(@Valid @RequestBody CouponPackageSaveReqVO couponPackageSaveReqVO) {
        couponPackageShareService.createCouponPackageShare(couponPackageSaveReqVO);
        return success(true);
    }

    /**
     * 推广详情
     */
    @GetMapping("/shareDetail")
    @Operation(summary = "优惠券包推广详情")
    //@PreAuthorize("@ss.hasPermission('package:share:query')")
    public CommonResult<CouponPackageShareRespVO> getShareDetail( CouponPackageDetailReqVO couponPackageDetailReqVO) {
        CouponPackageShareRespVO couponPackageShareRespVO =  couponPackageShareService.getPackageShareDetail(couponPackageDetailReqVO);
        return success(couponPackageShareRespVO);
    }

    /**
     * 单笔发放优惠卷
     */
    @PostMapping(value = "/issueCoupon")
    @Operation(summary = "单笔发放优惠卷")
    @PreAuthorize("@ss.hasPermission('package:coupon-package:issue')")
    //@PreAuthorize("@ss.hasPermission('package:share:update')")
    public CommonResult<Boolean> issueCoupon(@Valid @RequestBody CouponPackageSendAO couponPackageSendAO) {
        couponPackageShareService.issueCoupon(couponPackageSendAO);
        return success(true);
    }

    /**
     * 批量发放优惠卷
     */
    @PostMapping(value = "/importCouponExl")
    @Operation(summary = "批量发放优惠卷")
    @PreAuthorize("@ss.hasPermission('package:coupon-package:issue')")
    //@PreAuthorize("@ss.hasPermission('package:share:update')")
    public CommonResult<Boolean> importCouponExl(@RequestParam("file") MultipartFile file, @RequestParam("num") int num, @RequestParam("packageId") Long packageId) {
        if (file.isEmpty()) {
            ErrorCode errorCode = new ErrorCode(200,"该用户不存在,文件不存在");
            return error(errorCode, null, "该用户不存在,文件不存在");
        }

        couponPackageShareService.importCouponExl(file, num, packageId);
        return success(true);
    }

    /**
     * 下载用户模版
     */
    @PostMapping(value = "/downTemplate")
    @Operation(summary = "下载用户模版")
    public ResponseEntity<byte[]> downTemplate(HttpServletResponse response) throws IOException {
        // 创建一个空的列表，用于生成模板
        List<MemberExlVo> data = new ArrayList<>();
        // 使用EasyExcel生成Excel模板
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream, MemberExlVo.class).sheet("用户发放优惠卷包模板").doWrite(data);
        // 设置响应头
        // 获取文件名并正确编码
        String filename = "发放模板.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        // 使用RFC 5987标准构建Content-Disposition
        String contentDisposition = ContentDisposition.builder("attachment")
                .filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString();

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        // 返回响应
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    /**
     * 获取默认小图
     */
    //@PreAuthorize("@ss.hasPermi('system:coupon:query')")
    @GetMapping(value = "/getLittlePic")
    @Operation(summary = "获取默认小图")
    public CommonResult<String> getLittlePic() {
        return success( couponPackageShareService.getLittlePic());
    }


    /**
     * 获取默认大图
     */
    //@PreAuthorize("@ss.hasPermi('system:coupon:query')")
    @GetMapping(value = "/getLargePic")
    @Operation(summary = "获取默认大图")
    public CommonResult<String> getLargePic() {
        return success( couponPackageShareService.getLargePic());
    }


}
