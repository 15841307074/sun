package com.htyoudao.youdao.module.member.service.address;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.controller.app.address.vo.CheckWithinDeliveryRangeReqVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressReqMemberVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressReqVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressRespVO;

import java.util.List;
import java.util.Map;

/**
 * 微信小程序用户收货地址Service接口
 *
 * @author lbw
 * @date 2025-04-17
 */
public interface WxMemberAddressService {

    List<WxMemberAddressRespVO> getAddressListForMemberId(WxMemberAddressReqMemberVO wxMemberAddressVO);

    CommonResult<Integer> insertbyChat(WxMemberAddressReqVO wxMemberAddressVO);

    CommonResult<Integer> updateAddressByChat(WxMemberAddressReqVO wxMemberAddressVO);

    Integer deleteAddressByAddressId(WxMemberAddressReqVO wxMemberAddressReqVO);

    Map<String, Object> getAddressMapForMemberId(CheckWithinDeliveryRangeReqVO checkWithinDeliveryRangeReqVO);
}
