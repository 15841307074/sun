package com.htyoudao.youdao.module.promotion.api.activity;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMJDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMzDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivitySeckillRespDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityStoreTagUpdateDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftInventoryQuery;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftInventoryResult;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.SettlementActivitiesDTO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelNameDataRespVO;
import com.htyoudao.youdao.module.promotion.api.enums.coupon.CouponStoreTagSqlTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "RPC 服务 - 营销活动")
public interface ActivityApi {

    /** 一次查询结算所需的N件N折、满减满折和满赠活动。 */
    CommonResult<SettlementActivitiesDTO> selectSettlementActivities(
            @RequestParam("storeId") Long storeId,
            @RequestParam("commodityIds") Collection<Long> commodityIds
    );

    CommonResult<Map<Long, List<ActivityNjnzDTO>>> selectNjnzActivity(
        @RequestParam("storeId") Long storeId,
        @RequestParam("commodityIds") Collection<Long> commodityIds
    );

    CommonResult<Map<Long, List<ActivityMJDTO>>> selectMJActivity(
            @RequestParam("storeId") Long storeId,
            @RequestParam("commodityIds") Collection<Long> commodityIds
    );

    CommonResult<Map<Long, List<ActivityMzDTO>>> selectMZActivity(
            @RequestParam("storeId") Long storeId,
            @RequestParam("commodityIds") Collection<Long> commodityIds
    );

    /** 查询满赠赠品实时可用库存；-1 表示不限库存，null 表示库存缓存不存在。 */
    CommonResult<Integer> queryMzGiftInventory(@RequestParam("activityId") Long activityId,
                                                @RequestParam("storeId") Long storeId,
                                                @RequestParam("giftCommodityId") Long giftCommodityId);

    /**
     * 批量查询满赠赠品实时可用库存，结果与请求数量、顺序一致。
     * 该接口仅供结算展示，不锁定库存；inventory为null表示缓存缺失，-1表示不限库存。
     * Promotion内部会按活动库存模式和Redis Hash Key分组，通过HMGET减少RPC及Redis访问次数。
     */
    CommonResult<List<MzGiftInventoryResult>> queryMzGiftInventories(List<MzGiftInventoryQuery> queries);

    CommonResult<Boolean> checkMzCanParticipate(Long activityId, Long memberId);

    /** 提交订单锁定赠品库存，返回实际锁定数量（库存不足自动截断）。 */
    CommonResult<Integer> lockMzGiftInventory(Long activityId, Long storeId, Long memberId, String orderSn,
                                               Long giftCommodityId, Long giftSkuId, Integer quantity,
                                               Boolean checkUserLimit);

    /** 支付成功：锁定库存转已消耗。 */
    CommonResult<Boolean> confirmMzGiftInventory(String orderSn);

    /** 未支付取消/超时：释放锁定库存及参与次数。 */
    CommonResult<Boolean> releaseMzGiftInventory(String orderSn);

    /** 退款：恢复已消耗库存及参与次数。 */
    CommonResult<Boolean> refundMzGiftInventory(String orderSn);


    /**
     * 获取秒杀活动详情
     *
     * @param activityId 活动ID
     * @return
     */
    CommonResult<ActivitySeckillRespDTO> selectSeckillActivity(
        @RequestParam("storeId") Long activityId
    );


    /**
     * 获取秒杀商品库存
     *
     * @param activityId   活动ID
     * @param time         场次号
     * @param commodityIds 商品ID
     * @return
     */
    CommonResult<Map<Long, Integer>> seckillStock(
        @RequestParam("storeId") Long storeId,
        @RequestParam("activityId") Long activityId,
        @RequestParam("time") Integer time,
        @RequestParam("commodityIds") Collection<Long> commodityIds
    );


    /**
     * 校验小程序用户是否允许参加活动
     */
    CommonResult<Boolean> checkCanJoin(Long activityId);

    /**
     * 查询渠道名称
     * @return
     */
    CommonResult<String> selectChannelName(
            @RequestParam("channelId") Integer channelId
    );

    /**
     * 获取活动引导图
     *
     * @param activityId 活动ID
     * @return 引导图列表
     */
    List<String> getGuideImage(@RequestParam("activityId") Long activityId);

    /**
     *  通过活动ids获取活动names
     * @param activityIds 活动ID
     * @return Map<Long,String>
     */
    CommonResult<Map<Long,String>> selectActivityByIds(Set<Long> activityIds);

    /**
     * 获取渠道列表
     * @return
     */
    CommonResult<List<ActivityChannelNameDataRespVO>> getChannelList();

    /**
     * 门店标签变更后联动维护活动-门店绑定关系（activity_store）
     * <p>仅处理应用范围为标签（app_scope=1）的活动；门店范围（app_scope=0）的活动不处理。
     * <p>UPDATE：门店新增标签后，为引用了这些标签且尚无绑定记录的活动补充绑定。
     * <p>DELETE：门店移除标签后，若活动配置的标签集合均不再命中该门店（由 system 侧缓存判断），删除该活动与门店的绑定。
     *
     * @param updates 门店及其变更的标签列表
     * @param sqlType UPDATE 新增绑定 / DELETE 删除绑定
     */
    void updateActivityStoreByTagIdAndStoreId(List<ActivityStoreTagUpdateDTO> updates, CouponStoreTagSqlTypeEnum sqlType);

}
