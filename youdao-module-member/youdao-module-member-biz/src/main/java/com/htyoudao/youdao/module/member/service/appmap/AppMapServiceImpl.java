package com.htyoudao.youdao.module.member.service.appmap;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.dal.redis.map.MemberMapRedisDAO;
import com.htyoudao.youdao.module.member.framework.rpc.config.GeocoderKeyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_MAP_GET;

@Slf4j
@Service
public class AppMapServiceImpl implements AppMapService {

    private static final String GEOCODER_API_URL = "https://lts.maiyun.net/api/service/geocode";
    private static final String TENCENT_GEOCODER_API_URL = "https://apis.map.qq.com/ws/geocoder/v1/";
    private static final String MAIYUN_SUGGESTION_API_URL = "https://lts.maiyun.net/api/service/search";
    private static final String TENCENT_SUGGESTION_API_URL = "https://apis.map.qq.com/ws/place/v1/suggestion/";

    private final GeocoderKeyProperties geocoderConfig;
    private final MemberMapRedisDAO memberMapRedisDAO;

    public AppMapServiceImpl(GeocoderKeyProperties geocoderConfig, MemberMapRedisDAO memberMapRedisDAO) {
        this.geocoderConfig = geocoderConfig;
        this.memberMapRedisDAO = memberMapRedisDAO;
    }

    @Override
    public String geocoderV3(String lon, String lat) {
        try {
            return geocoderConfig.isMaiyunEnabled()
                    ? geocoderByMaiyun(lon, lat)
                    : geocoderByTencent(lon, lat);
        } catch (Exception e) {
            throw exception(WX_MEMBER_MAP_GET);
        }
    }

    @Override
    public String suggestionV3(String keyword, String region) {
        try {
            return geocoderConfig.isMaiyunEnabled()
                    ? suggestionByMaiyun(keyword)
                    : suggestionByTencent(keyword, region);
        } catch (Exception e) {
            throw exception(WX_MEMBER_MAP_GET);
        }
    }

    private String suggestionByTencent(String keyword, String region) {
        Long businessId = BusinessContextHolder.getBusinessId();
        String key = geocoderConfig.getConfig().get(businessId);
        if (key == null || key.isEmpty()) {
            throw exception(WX_MEMBER_MAP_GET);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        if (region != null && !region.isEmpty()) {
            params.put("region", region);
        }
        params.put("key", key);
        String resultJson = null;
        try {
            resultJson = HttpUtil.get(TENCENT_SUGGESTION_API_URL, params);
            JSONObject jsonObject = JSONObject.parseObject(resultJson);
            if (jsonObject == null || jsonObject.getIntValue("status") != 0) {
                throw exception(WX_MEMBER_MAP_GET);
            }
            JSONArray data = jsonObject.getJSONArray("data");
            return data == null ? "[]" : data.toJSONString();
        } catch (RuntimeException e) {
            Map<String, Object> logParams = new HashMap<>();
            logParams.put("keyword", keyword);
            if (region != null && !region.isEmpty()) {
                logParams.put("region", region);
            }
            log.error("腾讯地图提示词搜索调用异常，入参={}，响应={}", logParams, resultJson, e);
            throw e;
        }
    }

    private String suggestionByMaiyun(String keyword) {
        String key = geocoderConfig.getMaiyunKey();
        if (key == null || key.isEmpty()) {
            throw exception(WX_MEMBER_MAP_GET);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("address", keyword);
        String resultJson = null;
        try {
            try (HttpResponse response = HttpRequest.post(MAIYUN_SUGGESTION_API_URL)
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toJSONString())
                    .execute()) {
                resultJson = response.body();
            }
            JSONObject jsonObject = JSONObject.parseObject(resultJson);
            recordMaiyunError("suggestion", jsonObject);
            if (jsonObject == null || jsonObject.getIntValue("result") != 1) {
                throw exception(WX_MEMBER_MAP_GET);
            }
            return convertToTencentSuggestion(jsonObject.getJSONArray("list")).toJSONString();
        } catch (RuntimeException e) {
            log.error("迈云地图提示词搜索调用异常，入参={}，响应={}", requestBody, resultJson, e);
            throw e;
        }
    }

    /**
     * 将迈云 POI 列表转换成腾讯关键词输入提示接口的 data 数组结构。
     */
    private JSONArray convertToTencentSuggestion(JSONArray sourceList) {
        JSONArray result = new JSONArray();
        if (sourceList == null) {
            return result;
        }
        for (Object item : sourceList) {
            JSONObject source = (JSONObject) item;
            JSONObject address = source.getJSONObject("address");
            JSONObject context = address == null ? null : address.getJSONObject("context");
            JSONObject province = context == null ? null : context.getJSONObject("province");
            JSONObject city = context == null ? null : context.getJSONObject("city");
            JSONObject district = context == null ? null : context.getJSONObject("district");

            JSONObject target = new JSONObject();
            target.put("id", source.getString("id"));
            target.put("title", source.getString("name"));
            target.put("address", address == null ? "" : address.getString("name"));
            target.put("category", categoryNames(source.getJSONArray("categories")));
            target.put("type", 0);
            target.put("location", source.getJSONObject("point"));
            target.put("adcode", getCode(district));
            target.put("province", getName(province));
            target.put("city", getName(city));
            target.put("district", getName(district));
            result.add(target);
        }
        return result;
    }

    private String categoryNames(JSONArray categories) {
        if (categories == null) {
            return "";
        }
        return categories.stream()
                .map(item -> ((JSONObject) item).getString("name"))
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.joining(";"));
    }

    /**
     * 原腾讯地图逆地理实现，开关关闭时使用。
     */
    private String geocoderByTencent(String lon, String lat) {
        Long businessId = BusinessContextHolder.getBusinessId();
        String key = geocoderConfig.getConfig().get(businessId);
        if (key == null || key.isEmpty()) {
            throw exception(WX_MEMBER_MAP_GET);
        }

        String url = TENCENT_GEOCODER_API_URL + "?location=" + lat + "," + lon
                + "&key=" + key + "&get_poi=1";
        Map<String, Object> params = new HashMap<>();
        params.put("lon", lon);
        params.put("lat", lat);
        String resultJson = HttpUtil.get(url, params);
        JSONObject jsonObject = JSONObject.parseObject(resultJson);
        if (jsonObject == null || !"0".equals(jsonObject.getString("status"))) {
            log.error("腾讯地图逆地理入参：{} ,响应: {}", params, jsonObject);
            throw exception(WX_MEMBER_MAP_GET);
        }
        return jsonObject.getString("result");
    }

    /**
     * 迈云位置服务逆地理实现，开关开启时使用。
     */
    private String geocoderByMaiyun(String lon, String lat) {
        String key = geocoderConfig.getMaiyunKey();
        if (key == null || key.isEmpty()) {
            throw exception(WX_MEMBER_MAP_GET);
        }

        JSONObject point = new JSONObject();
        point.put("lng", Double.parseDouble(lon));
        point.put("lat", Double.parseDouble(lat));

        JSONObject requestBody = new JSONObject();
        requestBody.put("from", geocoderConfig.getMaiyunFrom());
        requestBody.put("point", point);

        String resultJson;
        try (HttpResponse response = HttpRequest.post(GEOCODER_API_URL)
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .body(requestBody.toJSONString())
                .execute()) {
            resultJson = response.body();
        } catch (RuntimeException e) {
            // Hutool 会包装底层连接或读取超时，需要沿异常链判断。
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof SocketTimeoutException) {
                    memberMapRedisDAO.incrementMaiyunTimeout("geocoder");
                    break;
                }
                if (cause == cause.getCause()) {
                    break;
                }
                cause = cause.getCause();
            }
            log.error("迈云地图逆地理调用异常，入参={}", requestBody, e);
            throw e;
        }
        JSONObject jsonObject = JSONObject.parseObject(resultJson);
        recordMaiyunError("geocoder", jsonObject);
        if (jsonObject == null || jsonObject.getIntValue("result") != 1) {
            if (jsonObject == null || jsonObject.getIntValue("result") != -5) {
                log.error("迈云地图逆地理入参 {} ,响应: {}", requestBody, jsonObject);
            }
            throw exception(WX_MEMBER_MAP_GET);
        }

        return convertToTencentResult(jsonObject).toJSONString();
    }

    /**
     * 累计两个接口返回的所有错误码，包括 -5。
     */
    private void recordMaiyunError(String api, JSONObject response) {
        Integer result = response == null ? null : response.getInteger("result");
        if (result == null || result == 1) {
            return;
        }
        memberMapRedisDAO.incrementMaiyunError(api, result);
    }

    /**
     * 保持 /v3/geocoder 原有的返回结构，避免现有小程序和内部 RPC 调用方受接口迁移影响。
     */
    private JSONObject convertToTencentResult(JSONObject source) {
        JSONObject address = source.getJSONObject("address");
        JSONObject context = address.getJSONObject("context");

        JSONObject country = context.getJSONObject("country");
        JSONObject province = context.getJSONObject("province");
        JSONObject city = context.getJSONObject("city");
        JSONObject district = context.getJSONObject("district");
        JSONObject township = context.getJSONObject("township");

        JSONObject addressComponent = new JSONObject();
        addressComponent.put("nation", getName(country));
        addressComponent.put("province", getName(province));
        addressComponent.put("city", getName(city));
        addressComponent.put("district", getName(district));
        addressComponent.put("street", getName(township));
        addressComponent.put("street_number", "");

        JSONObject adInfo = new JSONObject();
        adInfo.put("nation_code", getCode(country));
        adInfo.put("adcode", getCode(district));
        adInfo.put("city_code", getCode(city));
        adInfo.put("nation", getName(country));
        adInfo.put("province", getName(province));
        adInfo.put("city", getName(city));
        adInfo.put("district", getName(district));

        JSONObject result = new JSONObject();
        result.put("location", source.getJSONObject("point"));
        result.put("address", address.getString("name"));

        JSONObject formattedAddresses = new JSONObject();
        formattedAddresses.put("recommend", source.getString("name"));
        formattedAddresses.put("rough", address.getString("name"));
        result.put("formatted_addresses", formattedAddresses);

        result.put("address_component", addressComponent);
        result.put("ad_info", adInfo);
        return result;
    }

    private String getName(JSONObject area) {
        return area == null ? "" : area.getString("name");
    }

    private String getCode(JSONObject area) {
        return area == null ? "" : area.getString("code");
    }
}
