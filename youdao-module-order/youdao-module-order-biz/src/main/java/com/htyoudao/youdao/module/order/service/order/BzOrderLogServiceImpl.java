package com.htyoudao.youdao.module.order.service.order;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderLogDO;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderLogMapper;
import com.htyoudao.youdao.module.order.util.DateUtils;
import com.htyoudao.youdao.module.order.util.LogUtil;
import com.htyoudao.youdao.module.order.util.SHA1Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  订单日志 服务实现类
 * </p>
 *
 * @author zhangjihe
 * @since 2024-10-08
 */
@DS(DsNameConstants.SHARDING)
@Slf4j
@Service
public class BzOrderLogServiceImpl extends ServiceImpl<BzOrderLogMapper, BzOrderLogDO> implements BzOrderLogService {

    @Override
    public List<BzOrderLogDO> getOrderLogList(List<String> orderSns, LocalDateTime[] createTimes) {
        return baseMapper.selectList(orderSns, createTimes);
    }


    //重刷appid
    public static void main(String[] args) {

        // 1) 多个 custId
        List<String> custIds = Arrays.asList(
                "60000017082939"

        );

        // 2) 固定参数（除 custId / sign 外）
        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("agetId", "61000000472395");
        baseParams.put("payType", "02");
        baseParams.put("payWay", "01");
        baseParams.put("appid", "wx3b84773f5f12d87f");
        baseParams.put("version", "1.0.0");

        String url = "https://yyfsvxm.postar.cn/yyfsevr/addCust/appidConfig";
        String publicKey = "CLOUD_SECRET_REQUIRED";

        // 3) 汇总结果（custId -> 响应JSON / 或错误信息）
        Map<String, Object> resultMap = new LinkedHashMap<>();

        for (String custId : custIds) {
            try {
                // 每次循环都复制一份，避免 sign 污染 baseParams
                Map<String, String> params = new HashMap<>(baseParams);

                params.put("custId", custId);
                params.put("timeStamp", DateUtils.dateTimeNow());

                // 关键：每个 custId 都要重新算签名
                String signStr = SHA1Utils.ASCII(params, null);
                String sha256 = SHA1Utils.getSHA256(signStr);
                String sign = SHA1Utils.encrypt(publicKey, sha256);
                params.put("sign", sign);

                log.info("发起微信APPID配置请求 => custId={} | URL: {} | 参数: {}", custId, url, LogUtil.maskSensitiveParams(params));

                String response = HttpUtil.post(url, JSON.toJSONString(params));
                JSONObject jsonResponse = JSON.parseObject(response);

                log.info("微信APPID配置响应结果 => custId={} | {}", custId, LogUtil.maskSensitiveInfo(jsonResponse));

                resultMap.put(custId, jsonResponse);
            } catch (Exception e) {
                log.error("请求失败 => custId={}", custId, e);
                resultMap.put(custId, "ERROR: " + e.getMessage());
            }
        }

        log.info("全部 custId 调用完成 => {}", LogUtil.maskSensitiveInfo(resultMap));
    }
}
