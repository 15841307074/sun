package com.htyoudao.youdao.module.order.controller.app.order;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.excel.pojo.SearchAfterPage;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.OrderPageReqVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.OrderResVO;
import com.htyoudao.youdao.module.order.dal.es.BzOrderPointsDocument;
import com.htyoudao.youdao.module.order.service.job.JobService;
import com.htyoudao.youdao.module.order.service.order.BzOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


@Tag(name = "app - 活动订单", description = "活动弹幕")
@RestController
@RequestMapping("/order/app/activity")
public class ActivityOrderController {

    @Resource
    private BzOrderService bzOrderService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private JobService jobService;


    @GetMapping("/job/test")
    @PermitAll
    public CommonResult<String> jobTest() {
        jobService.reportTotalWeek();
        jobService.reportCommodityWeek();
        jobService.reportWarehouseWeek();
        jobService.reportStoreSalesWeek();
        jobService.reportStasticsWeek();
        jobService.reportAppSalesnumWeek();

        jobService.reportTotalMonth();
        jobService.reportWarehouseMonth();
        jobService.reportAppSalesnumMonth();
        jobService.reportCommodityMonth();
        jobService.reportStoreSalesMonth();
        jobService.reportStasticsMonth();
        jobService.reportBuyProportionMonth();

        return CommonResult.success("success");
    }

    @Operation(summary = "秒杀弹幕")
    @GetMapping("/seckill/buy/list")
    @PermitAll
    public CommonResult<List<String>> seckillBuyList(Long activityId, Long storeId, Integer discountType) {

        if (Objects.equals(discountType, 1)){
            try {
                String key = "claim_coupon_queue:" + activityId;
                // 获取最新的10条弹幕
                List<String> danmakuList = redisTemplate.opsForList().range(key, 0, 9);
                return CommonResult.success(danmakuList);
            } catch (Exception e) {
                return CommonResult.success(List.of());
            }
        }


        OrderPageReqVO pageReqVO = new OrderPageReqVO();
        pageReqVO.setActivityId(activityId);
        pageReqVO.setStoreId(storeId);
        pageReqVO.setIsStore(0);
        pageReqVO.setMobile("1");

        SearchAfterPage<OrderResVO> pageResult = bzOrderService.pOrderpage(pageReqVO);
        if (pageResult == null || CollectionUtils.isEmpty(pageResult.getList())) {
            return CommonResult.success(List.of());
        }

        List<String> list = pageResult.getList().stream()
                .map(OrderResVO::getTakeAwayTel)
                .filter(Objects::nonNull) // 过滤掉 null
                .filter(tel -> tel.matches("^1[3-9]\\d{9}$")) // 确保是11位手机号
                .map(tel -> tel.substring(7) + "会员 刚刚成功抢购") // 返回后4位（如 "****7097"）
                .toList();

        return CommonResult.success(list);
    }

    @GetMapping("/pointsCollect/list")
    @Operation(summary = "集点明细")
    public CommonResult<List<BzOrderPointsDocument>> pointsCollectPage(@RequestParam Long activityId) {
        return success(bzOrderService.pointsCollectPage(activityId));
    }

    @GetMapping("/pointsCollect/count")
    @Operation(summary = "集点点数")
    public CommonResult<Integer> pointsCollectCount(@RequestParam Long activityId) {
        return success(bzOrderService.pointsCollectCount(activityId));
    }
}
