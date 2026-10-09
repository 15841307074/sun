package com.htyoudao.youdao.module.system.api.storeinfo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDeliveryDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreSimpleResDto;
import com.htyoudao.youdao.module.system.api.storeinfo.StoreInfoApi;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageListReqVO;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageResVO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.util.string.StringUtils;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


@DubboService // 提供 RESTful API 接口，给 Feign 调用
public class StoreInfoApiImpl implements StoreInfoApi{

    @Resource
    private SystemStoreInfoService systemStoreInfoService;


    @Override
    public CommonResult<PageResult<StorePageResVO>> chooseStore(StorePageListReqVO pageReqVO) {
        com.htyoudao.youdao.module.system.controller.admin.store.vo.StorePageListReqVO storePageListReqVO = new com.htyoudao.youdao.module.system.controller.admin.store.vo.StorePageListReqVO();
        BeanUtils.copyProperties(pageReqVO,storePageListReqVO);
        PageResult<SystemStoreInfoDO> systemStoreInfoDOPageResult = systemStoreInfoService.chooseStore(storePageListReqVO);
        PageResult<StorePageResVO> pageResult = new PageResult<>();
        BeanUtils.copyProperties(systemStoreInfoDOPageResult,pageResult);
        return CommonResult.success(pageResult);
    }

    @Override
    public CommonResult<PageResult<StorePageResVO>> chooseStoreForTiktok(StorePageListReqVO pageReqVO) {
        com.htyoudao.youdao.module.system.controller.admin.store.vo.StorePageListReqVO storePageListReqVO = new com.htyoudao.youdao.module.system.controller.admin.store.vo.StorePageListReqVO();
        BeanUtils.copyProperties(pageReqVO,storePageListReqVO);
        PageResult<SystemStoreInfoDO> systemStoreInfoDOPageResult = systemStoreInfoService.chooseStoreForTiktok(storePageListReqVO);
        PageResult<StorePageResVO> pageResult = new PageResult<>();
        BeanUtils.copyProperties(systemStoreInfoDOPageResult,pageResult);
        return CommonResult.success(pageResult);
    }

    @Override
    public CommonResult<List<StoreInfoDTO>> getStoresByName(String storeName) {
        return success(systemStoreInfoService.getStoresByName(storeName));
    }

    @Override
    public CommonResult<PageResult<StoreInfoDTO>> getStoreInfoByStoreIds(List<Long> storeIds, PageParam pageParam) {
        return success(systemStoreInfoService.getStoreInfoByStoreIds(storeIds, pageParam));
    }
}
