package com.htyoudao.youdao.module.order.client;

import com.htyoudao.youdao.module.order.client.DTO.SyncCommodityDTO;
import com.htyoudao.youdao.module.order.client.VO.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 * 拼单
 * </p>
 *
 * @author zhangjihe
 * @since 2024-07-03
 */
@FeignClient(name = "admin-splicing")
public interface SplicingNettyClient {

    @PostMapping("/netty-splicing/splicing/syncMenuToOtherMember")
    ResultVO syncMenuToOtherMember(@RequestBody SyncCommodityDTO body);

    @PostMapping("/netty-splicing/splicing/finishOrderNotifyAll")
    ResultVO finishOrderNotifyAll(@RequestBody SyncCommodityDTO body);

    @PostMapping("/netty-splicing/splicing/cancelOrderNotifyAll")
    ResultVO cancelOrderNotifyAll(@RequestBody SyncCommodityDTO body);

    @PostMapping("/netty-splicing/splicing/lockOrderNotifyAll")
    ResultVO lockOrderNotifyAll(@RequestBody SyncCommodityDTO body);

    @PostMapping("/netty-splicing/splicing/unLockOrderNotifyAll")
    ResultVO unLockOrderNotifyAll(@RequestBody SyncCommodityDTO body);

    @PostMapping("/netty-splicing/splicing/feignTest")
    ResultVO feignTest(@RequestParam String a);
}
