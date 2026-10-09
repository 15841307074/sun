package com.htyoudao.youdao.module.system.api.storeinfo;

import com.fhs.core.trans.anno.AutoTrans;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDeliveryDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreSimpleResDto;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageListReqVO;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageResVO;
import com.htyoudao.youdao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * @author dht
 */
//@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 门店")
public interface StoreInfoApi {

//    String PREFIX = ApiConstants.PREFIX + "/store";

//    @PostMapping(PREFIX+"/choose/page")
    @Operation(summary = "查询门店")
    public CommonResult<PageResult<StorePageResVO>> chooseStore(@RequestBody StorePageListReqVO pageReqVO);

    /**
     * 获取抖音门店列表
     * @param  pageReqVO pageReqVO
     * @return CommonResult
     */
    CommonResult<PageResult<StorePageResVO>> chooseStoreForTiktok(StorePageListReqVO pageReqVO);


    @Operation(summary = "查询所有门店")
    CommonResult<List<StoreInfoDTO>> getStoresByName(String storeName);

    CommonResult<PageResult<StoreInfoDTO>> getStoreInfoByStoreIds(List<Long> storeIds, PageParam pageParam);
}
