package com.htyoudao.youdao.module.promotion.service.couponRedeem;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.ClaimTiktokCouponReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.couponRedeem.VO.RedeemDouyinCouponReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponRedeem.DouyinCouponRedeemRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponRedeem.DouyinCouponRedeemRecordMapper;
import com.htyoudao.youdao.module.promotion.service.douyin.DyClient;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.Certificate;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.DouYinApiResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.PrepareRequest;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.PrepareResponse;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-07-02
 */
@Service
public class DouyinCouponRedeemRecordServiceImpl extends ServiceImpl<DouyinCouponRedeemRecordMapper, DouyinCouponRedeemRecordDO> implements DouyinCouponRedeemRecordService {

    @Resource
    private final DyClient dyClient;

    @Resource
    private UserCouponApi userCouponApi;

    @DubboReference
    private StoreApi storeApi;

    public DouyinCouponRedeemRecordServiceImpl(DyClient dyClient) {
        this.dyClient = dyClient;
    }

    @Override
    public void redeemDouyinCoupon(RedeemDouyinCouponReqVO reqVO) {
        CommonResult<StoreDTO> storeResult = storeApi.getStoreByDouyinStoreId(reqVO.getDouyinStoreId());

        StoreDTO storeDTO = storeResult.getCheckedData();
        if (ObjectUtils.isEmpty(storeDTO)) {
            throw new ServiceException(DOUYIN_STORE_ID_NOT_MATCH);
        }

        //验券
        PrepareResponse data = this.getPrepareResponse(reqVO.getDouyinCouponCode(), reqVO.getShortLink(), reqVO.getDouyinStoreId().toString());

        Certificate certificate = data.getCertificates().get(0);
        DouyinCouponRedeemRecordDO douyinCouponRedeemRecordDO = new DouyinCouponRedeemRecordDO();
        douyinCouponRedeemRecordDO.setDouyinCouponCode(reqVO.getDouyinCouponCode());
        douyinCouponRedeemRecordDO.setShortLink(reqVO.getShortLink());
        douyinCouponRedeemRecordDO.setDouyinOrderId(data.getOrderId());
        douyinCouponRedeemRecordDO.setPlatformCouponId(Long.valueOf(certificate.getSku().getThirdSkuId()));
        douyinCouponRedeemRecordDO.setUserId(SecurityFrameworkUtils.getLoginUserId());

        Map<String, Object> respMap = new HashMap<>();
        respMap.put("verify_token", data.getVerifyToken());
        respMap.put("encrypted_code", certificate.getEncryptedCode());

        douyinCouponRedeemRecordDO.setPrepareResponse(JSON.toJSONString(respMap));
        douyinCouponRedeemRecordDO.setDouyinStoreId(reqVO.getDouyinStoreId());
        douyinCouponRedeemRecordDO.setBusinessId(BusinessContextHolder.getBusinessId());

        //验券记录保存
        try {
            this.save(douyinCouponRedeemRecordDO);
        } catch (Exception e) {
            if (this.isDuplicateKeyException(e)) {
                throw new ServiceException(DOUYIN_COUPON_REDEEM_REPEAT);
            }
            throw e;
        }

        ClaimTiktokCouponReqVO claimTiktokCouponReqVO = new ClaimTiktokCouponReqVO();
        claimTiktokCouponReqVO.setCouponId(douyinCouponRedeemRecordDO.getPlatformCouponId());
        claimTiktokCouponReqVO.setStoreId(storeDTO.getStoreId());
//        claimTiktokCouponReqVO.setStoreId(1332383611650048L);
        claimTiktokCouponReqVO.setVaildStartTime(new Date(certificate.getStartTime() * 1000));
        claimTiktokCouponReqVO.setExpirationTime(new Date(certificate.getExpireTime() * 1000));

        try {
            Long userCouponId = userCouponApi.claimTiktokCoupon(claimTiktokCouponReqVO);

            this.update(
                    new LambdaUpdateWrapper<DouyinCouponRedeemRecordDO>()
                            .eq(DouyinCouponRedeemRecordDO::getDouyinOrderId, douyinCouponRedeemRecordDO.getDouyinOrderId())
                            .set(DouyinCouponRedeemRecordDO::getUserCouponId, userCouponId)
            );
        } catch (Exception e) {
            e.printStackTrace();
            this.remove(
                    new LambdaUpdateWrapper<DouyinCouponRedeemRecordDO>()
                            .eq(DouyinCouponRedeemRecordDO::getDouyinOrderId, douyinCouponRedeemRecordDO.getDouyinOrderId())
            );
            if (e.getClass().isNestmateOf(ServiceException.class)) {
                throw (ServiceException) e;
            }
            throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    @Override
    public PrepareResponse getPrepareResponse(String douyinCouponCode, String shortLink, String douyinStoreId) {
        PrepareRequest request = new PrepareRequest();
        request.setCode(douyinCouponCode);
        request.setPoiId(douyinStoreId);
        request.setShortLink(shortLink);

        /**
         * 先调用抖音验券准备接口
         * 调用验券准备入参：
         * ?account_id=CLOUD_SECRET_REQUIRED&code=106146695982050&poi_id=7508966884516055055
         *
         * 响应体需要保留以下字段：
         * verifyToken
         * encryptedCode
         */
        DouYinApiResponse<PrepareResponse> prepareResponse = dyClient.prepare(request);
        if (prepareResponse.getExtra().getErrorCode() != 0) {
            throw new ServiceException(DOUYIN_JSON_PROCESSING.getCode(), prepareResponse.getExtra().getDescription());
        }

        PrepareResponse data = prepareResponse.getData();
        if (CollectionUtils.isEmpty(data.getCertificates())) {
            throw new ServiceException(DOUYIN_COUPON_PREPARE_FAILED);
        }
        return data;
    }

    /**
     * 判断是否是数据库唯一键冲突异常
     */
    private boolean isDuplicateKeyException(Throwable e) {
        // 针对不同数据库/驱动做兼容（以 MySQL 为例）
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof java.sql.SQLIntegrityConstraintViolationException || (cause.getMessage() != null && cause.getMessage().toLowerCase().contains("duplicate entry"))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
