package com.htyoudao.youdao.module.member.controller.app.pointsLog;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO.*;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.PointsExchangeDetailReqVO;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.PointsLogVO;
import com.htyoudao.youdao.module.member.service.pointsLog.IPointsLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "app - 积分记录")
@RestController
@RequestMapping("/member/points-log")
public class AppPointsLogController {

    @Resource
    private IPointsLogService pointsLogService;


    @Operation(summary = "获取用户总积分")
    @PostMapping("/getMemberAllPoints")
    public CommonResult<WxPointsLogAndAllPointsDTO> getMemberAllPoints(@RequestBody PointsLogVO pointsLogVO) {

        WxPointsLogAndAllPointsDTO wxPointsLogAndAllPointsDTO = pointsLogService.getMemberAllPoints(pointsLogVO);

        return CommonResult.success(wxPointsLogAndAllPointsDTO);
    }

    @Operation(summary = "小程序根据用户id获取积分兑换记录列表")
    @PostMapping("/wxGetPointsLogDetailList")
    public CommonResult<List<WxPointsLogDetailDTO>> wxGetPointsLogDetailList(@RequestBody PointsLogVO pointsLogVO) {
        List<WxPointsLogDetailDTO> wxPointsLogDetailDTOList = pointsLogService.wxGetPointsLogDetailList(pointsLogVO);
        return success(wxPointsLogDetailDTOList);
    }

    /**
     * 查询小程序积分商品兑换详情。
     */
    @Operation(summary = "小程序查询积分商品兑换详情")
    @PostMapping("/getExchangeDetail")
    public CommonResult<WxPointsLogDetailDTO> getExchangeDetail(
            @Valid @RequestBody PointsExchangeDetailReqVO reqVO) {
        return success(pointsLogService.getExchangeDetail(reqVO));
    }


    @Operation(summary = "小程序根据用户 id查询积分明细")
    @PostMapping("/getListByMemberId")
    public CommonResult<WxPointsLogAndAllPointsDTO> getListByMemberId(@RequestBody PointsLogVO pointsLogVO) {
        WxPointsLogAndAllPointsDTO wxPointsLogAndAllPointsDTO = pointsLogService.getListByMemberId(pointsLogVO);
        return CommonResult.success(wxPointsLogAndAllPointsDTO);
    }

    /**
     * 小程序通过购买积分商品生成订单记录
     */

    /**
     * 小程序通过购买积分商品生成订单记录
     */
    @PostMapping("/productInsert")
    //@Log(title = "小程序通过购买积分商品生成订单记录", businessType = BusinessType.INSERT, systemType = SystemType.HBGC)
    public CommonResult<Boolean> productInsert(@RequestBody PointsLogEditReqVO pointsLogVO) {
        pointsLogService.productInsert(pointsLogVO);
        return success(true) ;
    }
}
