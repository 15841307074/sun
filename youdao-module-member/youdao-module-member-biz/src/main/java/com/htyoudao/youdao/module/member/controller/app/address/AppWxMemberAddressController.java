package com.htyoudao.youdao.module.member.controller.app.address;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.member.controller.app.address.vo.CheckWithinDeliveryRangeReqVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressReqMemberVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressReqVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressRespVO;
import com.htyoudao.youdao.module.member.dal.redis.address.MemberAddressRedisDAO;
import com.htyoudao.youdao.module.member.service.address.WxMemberAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 微信小程序用户收货地址Controller
 *
 * @author lbw
 * */


@Tag(name = "app 我的 - 地址管理")
@RestController
@RequestMapping("/member/address")
@Validated
public class AppWxMemberAddressController {

    @Resource
    private WxMemberAddressService wxMemberAddressService;
    @Resource
    private MemberAddressRedisDAO memberAddressRedisDAO;

    @PostMapping("/getAddressListForMemberId")
    @Operation(summary = "获取用户收货地址")
    public CommonResult<List<WxMemberAddressRespVO>> getAddressListForMemberId(@Valid @RequestBody WxMemberAddressReqMemberVO wxMemberAddressReqMemberVO) {
        List<WxMemberAddressRespVO> wxMemberAddressVOList = wxMemberAddressService.getAddressListForMemberId(wxMemberAddressReqMemberVO);
        return success(wxMemberAddressVOList);
    }

    @PostMapping("/insertAddress")
    @Operation(summary = "新增地址")
    public CommonResult<Integer> insertByChat(@Valid @RequestBody WxMemberAddressReqVO wxMemberAddressReqVO) {

        memberAddressRedisDAO.delete(SecurityFrameworkUtils.getLoginUsername());
        return wxMemberAddressService.insertbyChat(wxMemberAddressReqVO);
    }

    @PostMapping("/updateAddress")
    @Operation(summary = "修改用户管理地址")
    public CommonResult<Integer> updateAddressByChat(@Valid @RequestBody WxMemberAddressReqVO wxMemberAddressReqVO) {
        memberAddressRedisDAO.delete(SecurityFrameworkUtils.getLoginUsername());
        return wxMemberAddressService.updateAddressByChat(wxMemberAddressReqVO);
    }

    /**
     * 小程序用户逻辑删除收货地址
     *
     * @param wxMemberAddressReqVO
     * @return
     */
    @PostMapping("/deleteAddressByAddressId")
    @Operation(summary = "小程序用户逻辑删除收货地址")
    public CommonResult<Integer> deleteAddressByAddressId(@RequestBody WxMemberAddressReqVO wxMemberAddressReqVO) {
        memberAddressRedisDAO.delete(SecurityFrameworkUtils.getLoginUsername());
        return success(wxMemberAddressService.deleteAddressByAddressId(wxMemberAddressReqVO));
    }

    @PostMapping("/getAddressMapForMemberId")
    @Operation(summary = "获取用户收货地址可配送与不可配送")
    public CommonResult<Map<String,Object>> getAddressMapForMemberId(@Valid @RequestBody CheckWithinDeliveryRangeReqVO checkWithinDeliveryRangeReqVO) {
        Map<String,Object> maps = wxMemberAddressService.getAddressMapForMemberId(checkWithinDeliveryRangeReqVO);
        return success(maps);
    }
}
