package com.htyoudao.youdao.module.order.controller.app.sq.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.order.controller.app.sq.DO.SqState;
import com.htyoudao.youdao.module.order.controller.app.sq.SqStateMapper;
import com.htyoudao.youdao.module.order.controller.app.sq.VO.ProcessCodesResp;
import com.htyoudao.youdao.module.order.controller.app.sq.utils.DateUtils;
import com.htyoudao.youdao.module.order.controller.app.sq.utils.LogUtil;
import com.htyoudao.youdao.module.order.controller.app.sq.utils.SignUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqProcessServiceImpl implements SqProcessService {

    private static final String APPID_CONFIG_URL = "https://yyfsvxm.postar.cn/yyfsevr/addCust/appidConfig";
    private static final String APPID_CONFIG_PUBLIC_KEY = "CLOUD_SECRET_REQUIRED";

    private final SqStateMapper sqStateMapper;

    @Override
    public ProcessCodesResp processCodes(List<String> codes) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        if (codes == null || codes.isEmpty()) {
            return ProcessCodesResp.builder().resultMap(resultMap).build();
        }

        for (String code : codes) {
            if (code == null || code.isBlank()) {
                resultMap.put(String.valueOf(code), "SKIP: blank code");
                continue;
            }

            try {
                Object one = processOneWithStateCheck(code.trim());
                resultMap.put(code, one);
            } catch (Exception e) {
                log.error("处理失败 => code={}", code, e);
                resultMap.put(code, "ERROR: " + e.getMessage());
            }
        }

        return ProcessCodesResp.builder().resultMap(resultMap).build();
    }

    /**
     * 先查 sq_state：state=1 跳过；否则执行处理；成功后标记 state=1
     */
    @Transactional(rollbackFor = Exception.class)
    public Object processOneWithStateCheck(String code) {
        SqState stateRow = sqStateMapper.selectOne(
                new LambdaQueryWrapper<SqState>()
                        .eq(SqState::getCode, code)
                        .eq(SqState::getDeleted, 0)
                        .last("limit 1")
        );

        if (stateRow != null && Objects.equals(stateRow.getState(), 1)) {
            log.info("已处理，跳过 => code={}", code);
            return "SKIP: already processed code:" + code;
        }

        // 未处理：执行你的处理逻辑（把 main 中对 custId 的调用封装）
        JSONObject remoteResp = callRemote(code);

        // 只有成功才标记已处理（这里你可以按实际返回码判断成功）
        markProcessed(code, stateRow);

        return remoteResp;
    }

    /**
     * 封装 main 中的 HTTP 调用逻辑：code 对应原 main 里的 custId
     */
    private JSONObject callRemote(String code) {
        Map<String, String> params = buildBaseParams();

        // main 里的 custId
        params.put("custId", code);
        params.put("timeStamp", DateUtils.dateTimeNow());

        // 每个 code 重新算 sign
        String signStr = SignUtils.asciiKvJoin(params);
        String sha256 = SignUtils.sha256Hex(signStr);
        String sign = SignUtils.rsaEncryptBase64(
                "CLOUD_SECRET_REQUIRED"
                , sha256);

        params.put("sign", sign);

        log.info("发起切换请求 => code={} | URL: {} | 参数: {}",
                code, "https://yyfsvxm.postar.cn/yyfsevr/custConfig/config", LogUtil.maskSensitiveParams(params));

        String response = HttpUtil.post("https://yyfsvxm.postar.cn/yyfsevr/custConfig/config", JSON.toJSONString(params));
        JSONObject jsonResponse = JSON.parseObject(response);

        log.info("响应结果 => code={} | {}",
                code, LogUtil.maskSensitiveInfo(jsonResponse));

        return jsonResponse;
    }

    private Map<String, String> buildBaseParams() {
        Map<String, String> baseParams = new HashMap<>(16);
        baseParams.put("agetId", "61000000472395");
        baseParams.put("bakType", "01");
        baseParams.put("wxQdh", "863238759");
        baseParams.put("mAppid", "64010200012026020616354800002664");
        baseParams.put("version", "1.0.0");
        return baseParams;
    }

    private void markProcessed(String code, SqState stateRow) {
        if (stateRow == null) {
            SqState ins = new SqState();
            ins.setCode(code);
            ins.setState(1);
            ins.setDeleted(0);
            sqStateMapper.insert(ins);
            return;
        }

        stateRow.setState(1);
        sqStateMapper.updateById(stateRow);
    }

    @Override
    public ProcessCodesResp processCodesWithAppid(List<String> codes, String appid) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        if (codes == null || codes.isEmpty()) {
            return ProcessCodesResp.builder().resultMap(resultMap).build();
        }

        for (String code : codes) {
            if (code == null || code.isBlank()) {
                resultMap.put(String.valueOf(code), "SKIP: blank code");
                continue;
            }

            try {
                JSONObject response = callAppidConfigRemote(code.trim(), appid);
                resultMap.put(code, response);
            } catch (Exception e) {
                log.error("请求失败 => custId={}", code, e);
                resultMap.put(code, "ERROR: " + e.getMessage());
            }
        }

        log.info("全部 custId 调用完成 => {}", LogUtil.maskSensitiveInfo(resultMap));
        return ProcessCodesResp.builder().resultMap(resultMap).build();
    }

    private JSONObject callAppidConfigRemote(String custId, String appid) {
        Map<String, String> params = buildAppidConfigBaseParams(appid);
        params.put("custId", custId);
        params.put("timeStamp", DateUtils.dateTimeNow());

        String signStr = SignUtils.asciiKvJoin(params);
        String sha256 = SignUtils.sha256Hex(signStr);
        String sign = SignUtils.rsaEncryptBase64(APPID_CONFIG_PUBLIC_KEY, sha256);
        params.put("sign", sign);

        log.info("发起微信APPID配置请求 => custId={} | URL: {} | 参数: {}",
                custId, APPID_CONFIG_URL, LogUtil.maskSensitiveParams(params));

        String response = HttpUtil.post(APPID_CONFIG_URL, JSON.toJSONString(params));
        JSONObject jsonResponse = JSON.parseObject(response);
        log.info("微信APPID配置响应结果 => custId={} | {}",
                custId, LogUtil.maskSensitiveInfo(jsonResponse));
        return jsonResponse;
    }

    private Map<String, String> buildAppidConfigBaseParams(String appid) {
        Map<String, String> baseParams = new HashMap<>(8);
        baseParams.put("agetId", "61000000472395");
        baseParams.put("payType", "02");
        baseParams.put("payWay", "01");
        baseParams.put("appid", appid);
        baseParams.put("version", "1.0.0");
        return baseParams;
    }
}
