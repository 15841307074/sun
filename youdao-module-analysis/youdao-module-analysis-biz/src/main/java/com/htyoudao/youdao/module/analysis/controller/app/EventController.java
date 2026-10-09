package com.htyoudao.youdao.module.analysis.controller.app;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.dal.es.AdEvent;
import com.htyoudao.youdao.module.analysis.dal.es.AdExposureItem;
import com.htyoudao.youdao.module.analysis.dal.es.BaseEvent;
import com.htyoudao.youdao.module.analysis.enums.AdEventType;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.service.IEventService;
import com.htyoudao.youdao.module.analysis.service.impl.AsyncAdEventServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "C端埋点事件")
@RestController
@RequestMapping("/analysis/event")
public class EventController {

    @Resource
    private IEventService eventService;

    @Resource
    private AsyncAdEventServiceImpl asyncAdEventService;

    @Operation(summary = "增加")
    @PostMapping("/add")
    @PermitAll
    public CommonResult addEvent(@RequestBody @Valid BaseEvent event) {
        if (event.getTimestamp() == null){
            event.setTimestamp(new Date());
        }
        if (StringUtils.isBlank(event.getChannel())){
            event.setChannel("1000000002");
        }
        event.setBusinessId(BusinessContextHolder.getBusinessId());
        eventService.add(event);
        return CommonResult.success(true);
    }

    @Operation(summary = "广告埋点增加")
    @PostMapping("/ad-add")
    @PermitAll
    public CommonResult addAdEvent(@RequestBody @Valid AdEvent event) {
        if (event.getTimestamp() == null){
            event.setTimestamp(new Date());
        }
        // 事件类型必须属于广告事件
        if (!AdEventType.isAdEvent(event.getEventType())) {
            return CommonResult.error(ErrorCodeConstants.AD_EVENT_TYPE_INVALID);
        }
        // 批量曝光逻辑：eventType 为 ad_exposure 且 exposureList 不为空
        if (AdEventType.AD_EXPOSURE.getCode().equals(event.getEventType())
                && event.getExposureList() != null && !event.getExposureList().isEmpty()) {
            Long businessId = BusinessContextHolder.getBusinessId();
            // 先校验所有 item，再批量构建 AdEvent 列表
            List<AdExposureItem> exposureList = event.getExposureList();
            for (AdExposureItem item : exposureList) {
                // 广告ID非空校验
                if (item.getAdId() == null) {
                    return CommonResult.error(ErrorCodeConstants.AD_ID_EMPTY);
                }
                // 广告位序号必须在1-14内
                if (item.getAdInfoPosition() == null || item.getAdInfoPosition() < 1 || item.getAdInfoPosition() > 15) {
                    return CommonResult.error(ErrorCodeConstants.AD_POSITION_INVALID);
                }
            }
            // 批量构建 AdEvent 列表，异步发送 Kafka
            List<AdEvent> batchEvents = new ArrayList<>(exposureList.size());
            for (AdExposureItem item : exposureList) {
                AdEvent batchEvent = AdEvent.builder()
                        .from(event.getFrom())
                        .eventType(event.getEventType())
                        .timestamp(event.getTimestamp())
                        .storeId(event.getStoreId())
                        .memberId(event.getMemberId())
                        .businessId(businessId)
                        .adId(item.getAdId())
                        .adName(item.getAdName())
                        .adInfoPosition(item.getAdInfoPosition())
                        .build();
                batchEvents.add(batchEvent);
            }
            // 异步批量发送到 Kafka（整批一次序列化为 JSON 数组消息，单次 send），不阻塞 HTTP 线程
            asyncAdEventService.offerBatch(batchEvents);
            return CommonResult.success(true);
        }
        // 广告ID非空校验
        if (event.getAdId() == null) {
            return CommonResult.error(ErrorCodeConstants.AD_ID_EMPTY);
        }
        // 广告位序号必须在1-14内
        if (event.getAdInfoPosition() == null || event.getAdInfoPosition() < 1 || event.getAdInfoPosition() > 15) {
            return CommonResult.error(ErrorCodeConstants.AD_POSITION_INVALID);
        }
        // 停留时长负数或超过一天视为无效
        if (event.getStayMs() != null && (event.getStayMs() < 0 || event.getStayMs() > 86400000L)) {
            event.setStayMs(null);
        }
        event.setBusinessId(BusinessContextHolder.getBusinessId());
        // 批量曝光列表不写入 ES
        event.setExposureList(null);
        // 异步发送到 Kafka（offer 内部复用 offerBatch，同样以 JSON 数组格式发送），不阻塞 HTTP 线程
        asyncAdEventService.offer(event);
        return CommonResult.success(true);
    }

//    @Operation(summary = "test")
//    @PostMapping("/test")
//    @PermitAll
//    public CommonResult test() {
//        eventService.queryMemberIDs(EventType.SHARE,  LocalDateTime.now().minusDays(-1),LocalDateTime.now());
//        return CommonResult.success(true);
//    }

}
