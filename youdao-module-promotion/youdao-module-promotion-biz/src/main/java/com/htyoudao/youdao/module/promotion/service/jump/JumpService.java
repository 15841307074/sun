package com.htyoudao.youdao.module.promotion.service.jump;

import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.AppCodeReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.WechatJumpReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.WechatJumpRespVO;

/**
 * @author dht
 */
public interface JumpService {
    /**
     * 小程序跳转
     * @param wechatJumpReqVO wechatJumpReqVO
     * @return WechatJumpRespVO
     */
    WechatJumpRespVO wechatJump(WechatJumpReqVO wechatJumpReqVO);

    byte[] getUnlimitedMiniProgramCode(AppCodeReqVO appCodeReqVO);
}
