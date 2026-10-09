package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon;


import com.alibaba.excel.EasyExcel;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.VO.GoodCouponCardVO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponDataDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.*;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.UserRestrictionsEnum;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.FILE_IS_EMPTY;

/**
 * @author dht
 */
@Tag(name = "管理后台 - 优惠券")
@RestController("goodCouponController")
@RequestMapping("/promotion/good-coupon")
@Validated
@Slf4j
public class GoodCouponController {

    @Resource
    private GoodCouponService goodCouponService;

    /**
     * 查询优惠券列表
     */
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('good:coupon:page')")
    @Operation(summary = "查询优惠券列表")
    public CommonResult<PageResult<GoodCouponPageRespVO>> list(@Valid
                                                        GoodCouponPageReqVO goodCoupon) {
        return success(goodCouponService.selectGoodCouponListPage(goodCoupon));
    }

    @PostMapping("/create")
    @Operation(summary = "创建优惠券")
    @PreAuthorize("@ss.hasPermission('good:coupon:create')")
    public CommonResult<Long> createCoupon(@Valid @RequestBody GoodCouponSaveReqVO createReqVO) {
        return success(goodCouponService.createCoupon(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新优惠券")
    @PreAuthorize("@ss.hasPermission('good:coupon:update')")
    public CommonResult<Integer> updateCoupon(@Valid @RequestBody GoodCouponSaveReqVO updateReqVO) {
        return success(goodCouponService.updateCoupon(updateReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除优惠券")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('good:coupon:delete')")
    public CommonResult<Boolean> deleteCoupon(@RequestParam("id") Long id) {
        goodCouponService.deleteCoupon(id);
        return success(true);
    }

    @GetMapping("/getById")
    @Operation(summary = "获得优惠券")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('good:coupon:info')")
    public CommonResult<GoodCouponRespVO> getCoupon(@RequestParam("id") Long id) {
        return success(goodCouponService.getCouponById(id));
    }

    @Operation(summary = "优惠券上下架")
    @PutMapping("/updateIsGround")
    @PreAuthorize("@ss.hasPermission('good:coupon:updateIsGround')")
    public CommonResult<Integer> updateIsGround(@RequestParam("id") Long id, @RequestParam(value = "isGround",required = true) Integer isGround) {
        return success(goodCouponService.updateIsGround(id));
    }

    @PutMapping("/editCouponNum")
    @Operation(summary = "修改优惠券数量")
    public CommonResult<Integer> editCouponNum(@RequestBody CouponNumUpdateReqVO couponNumUpdateReqVO) {
        return success(goodCouponService.editCouponNum(couponNumUpdateReqVO));
    }

    @PostMapping(value = "/copy")
    @Operation(summary = "复制")
    public CommonResult<Long> copy(@RequestParam("id") Long id) {
        return success(goodCouponService.copy(id));
    }

    @PutMapping("/rebindCommodity")
    @Operation(summary = "重新绑定商品")
    public CommonResult<Integer>  rebindCommodity(@RequestBody RebindCommodityReqVO rebindCommodity) {
        return success(goodCouponService.rebindCommodity(rebindCommodity));
    }

    /**
     * 优惠券页面 优惠券的数据
     */
    @Operation(summary = "优惠券页面 优惠券使用的数据")
    @GetMapping(value = "/selectDataById")
    public CommonResult<GoodCouponDateRespVO> selectDataById(@RequestParam(value = "id") Long id) {
        return success(goodCouponService.selectDataById(id));
    }

    /**
     * 优惠券页面 优惠券的数据v2
     */
    @Operation(summary = "优惠券页面 优惠券使用的数据 按门店查询")
    @GetMapping(value = "/selectDataById/v2")
    public CommonResult<GoodCouponDateRespV2VO> selectDataByIdV2(GoodCouponDateReqVO goodCouponDateReqVO) {
        return success(goodCouponService.selectDataByIdV2(goodCouponDateReqVO));
    }


    @GetMapping(value = "/shareDetail")
    @Operation(summary = "推广详情")
    public CommonResult<CouponShareRespVO> getShareDetail(ShareDetailReqVO shareDetailReqVO) {
        return success(goodCouponService.getShareDetail(shareDetailReqVO));
    }

    @PostMapping(value = "/addShare")
    @Operation(summary = "新建/修改推广")
    public CommonResult<Boolean> addShare(@RequestBody ShareSaveReqVO shareSaveReqVO) {
        return success(goodCouponService.addShare(shareSaveReqVO));
    }

    /**
     * 活动优惠卷下拉列表
     */
    @GetMapping("/getCouponListByActivity")
    @Operation(summary = "活动优惠卷下拉列表")
    public CommonResult<List<GoodCouponRespVO>> getCouponListByActivity() {
        return success(goodCouponService.getCouponListByActivity());
    }


    /**
     * 积分商城优惠卷下拉列表
     */
    @GetMapping("/getCouponListByIntegral")
    @Operation(summary = "积分商城优惠卷下拉列表")
    public CommonResult<List<GoodCouponRespVO>> getCouponListByIntegral() {
        return success(goodCouponService.getCouponListByIntegral());
    }

    @PostMapping(value = "/issueCoupon")
    @Operation(summary = "单笔发放优惠卷")
    @PreAuthorize("@ss.hasPermission('good:coupon:issue')")
    public CommonResult<Boolean> issueCoupon(@RequestBody IssueCouponReqVO issueCouponReqVO) {
        return success(goodCouponService.issueCoupon(issueCouponReqVO));
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
        EasyExcel.write(outputStream, MemberExlVo.class).sheet("用户发放优惠卷模板").doWrite(data);
        // 设置响应头
        // 获取文件名并正确编码
        String filename = "发放模板.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20"); // 空格特殊处理
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
     * 批量发放优惠卷
     */
    @PostMapping(value = "/importCouponExl")
    @Operation(summary = "批量发放优惠卷")
    @PreAuthorize("@ss.hasPermission('good:coupon:issue')")
    public CommonResult<Boolean> importCouponExl(@RequestParam("file") MultipartFile file, @RequestParam("num") int num, @RequestParam("couponId") Long couponId) {
        if (file.isEmpty()) {
           throw exception(FILE_IS_EMPTY);
        }
        return success(goodCouponService.importCouponExl(file, num, couponId));
    }

    @GetMapping(value = "/getLittlePic")
    @Operation(summary = "获取默认小图")
    public CommonResult<String> getLittlePic() {
        return success(goodCouponService.getLittlePic());
    }


    /**
     * 获取默认大图
     */
    @GetMapping(value = "/getLargePic")
    @Operation(summary = "获取默认大图")
    public CommonResult<String> getLargePic() {
        return success(goodCouponService.getLargePic());
    }


    @GetMapping("/goodCouponPage")
    @Operation(summary = "查询优惠券窗口  券包")
    @PreAuthorize("@ss.hasPermission('good:coupon:page')")
    public CommonResult<PageResult<GoodCouponRespVO>> goodCouponListPage(@RequestParam(value = "pageNum") Integer pageNum,
                                                                   @RequestParam(value = "pageSize") Integer pageSize,
                                                                   @RequestParam(value = "couponName", required = false) String couponName,
                                                                   @RequestParam(value = "remark", required = false) String remark) {
        PageResult<GoodCouponRespVO> page = goodCouponService.goodCouponPage(pageNum,pageSize,couponName,remark);
        return success(page);
    }

    @PostMapping("/getGoodCouponPage")
    @Operation(summary = "查询优惠券窗口")
    @PreAuthorize("@ss.hasPermission('good:coupon:page')")
    public CommonResult<PageResult<GoodCouponRespVO>> getGoodCouponPage(@RequestBody GoodCouponGetPageReqVO goodCouponPageReqVO) {

        PageResult<GoodCouponRespVO> page = goodCouponService.getGoodCouponPage(goodCouponPageReqVO);
        return success(page);
    }

    @GetMapping("/userCouponInfo")
    @Operation(summary = "优惠卷详情")
    @PreAuthorize("@ss.hasPermission('good:coupon:info')")
    public CommonResult<GoodCouponRespVO> goodCouponInfo(@RequestParam("id") Long id) {
        GoodCouponRespVO goodCouponInfo = goodCouponService.goodCouponInfo(id);
        return CommonResult.success(goodCouponInfo);
    }

    @PostMapping("/nocCouponStoreNum")
    @Operation(summary = "定时同步优惠券门店领取数量")
    public CommonResult<Boolean> nocCouponStoreNum() {
        goodCouponService.nocCouponStoreNum();
        return CommonResult.success(true);
    }



    @PostMapping("/updateGoodCoupon")
    @Operation(summary = "更新优惠券（远程调用）")
    public CommonResult<Boolean> updateGoodCoupon(@RequestBody GoodCouponCardVO cardVO) {
        return goodCouponService.updateGoodCoupon(cardVO);
    }

    /**
     * 积分商城优惠券列表
     */
    @PostMapping("/couponPageWithPoints")
    @Operation(summary = "查询优惠券全部列表")
    @PermitAll
    public CommonResult<PageResult<GoodCouponPageRespVO>> couponPageWithPoints(
            @RequestBody GoodCouponPageReqVO reqVO) {
        return success(goodCouponService.couponPageWithPoints(reqVO));
    }


    @Operation(summary = "根据会员等级 定时发放优惠券")
    @PermitAll
    @PostMapping(value = "/scheduledSend")
    public CommonResult<Void> scheduledSend() {
        return success(goodCouponService.scheduledSend());
    }
    @GetMapping("/getByIdCoupon")
    @Operation(summary = "获取优惠卷信息及门店列表")
    public CommonResult<GoodCouponDataDTO> getByIdCoupon(@RequestParam("couponId") Long couponId) {
        GoodCouponDataDTO goodCouponDataDTO= goodCouponService.getByIdCoupon(couponId);
        return success(goodCouponDataDTO);
    }


    @Operation(summary = "pc端获取选择优惠券领取限制")
    @PostMapping("/getUserRestrictions")
    public CommonResult<String> getUserRestrictions(){
        return success(UserRestrictionsEnum.getEle());
    }

    @PutMapping("/updateAllH5")
    @Operation(summary = "更新所有h5")
    @PermitAll
    @DataPermission
    public CommonResult<Integer> updateAllH5() {
        return success(goodCouponService.updateAllH5());
    }


    @GetMapping("/testMemberDay")
    //@PreAuthorize("@ss.hasPermission('good:coupon:page')")
    @Operation(summary = "testMemberDay")
    @PermitAll
    public void testMemberDay() {
          goodCouponService.memberDayCoupon();

    }

    @GetMapping("/getCommunityQrImage")
    @Operation(summary = "获取社群二维码")
    public CommonResult<List<String>> getCommunityQrImage(Long couponId) {
        return success(goodCouponService.getCommunityQrImage(couponId));
    }
}