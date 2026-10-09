package com.htyoudao.youdao.module.system.controller.admin.sysconfig;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StorePageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreResVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.YlbConfigActionVO;
import com.htyoudao.youdao.module.system.controller.admin.sysconfig.vo.SysConfigReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.sysconfig.YlbStoreConfigDO;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.service.sysconfig.SysConfigService;
import com.htyoudao.youdao.module.system.service.sysconfig.YlbStoreConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.constants.RedisKeyConstants.PUSH_HASH_KEY;
import static com.htyoudao.youdao.framework.common.constants.RedisKeyConstants.REQ_HASH_KEY;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.SYS_CONFIG_KEY_NOT_EXIST;


@Tag(name = "系统配置")
@RestController
@RequestMapping("/system/sys-config")
@Validated
public class SysConfigController {

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    @Resource
    private YlbStoreConfigService ylbStoreConfigService;

    @Resource
    protected StringRedisTemplate stringRedisTemplate;

    @GetMapping(value = "/getBykeys")
    public CommonResult<Map<String, String>> getBykeys(@RequestParam(value = "keyList", required = false) List<String> keyList) {
        return success(sysConfigService.getBykeys(keyList));
    }

    @PostMapping(value = "/editConfig")
    public CommonResult<Boolean> editConfig(@RequestBody SysConfigReqVO sysConfig) {
        if (sysConfigService.checkSysConfigByConfigKey(sysConfig)) {
            String msg = String.format(SYS_CONFIG_KEY_NOT_EXIST.getMsg(), sysConfig.getConfigKey());
            return error(new ErrorCode(SYS_CONFIG_KEY_NOT_EXIST.getCode(), msg));
        }
        return success(sysConfigService.updateSysConfig(sysConfig));
    }

    /**
     * 门店列表携带云喇叭开关
     */
    @GetMapping("/ylbConfig/page")
    @Operation(summary = "门店列表携带云喇叭开关")
    public CommonResult<PageResult<StoreResVO>> getYlbConfigPage(@Valid StorePageReqVO pageReqVO) {
        pageReqVO.setOrgId(1945651690735878144L);//写死汉堡工厂
        // 获得用户分页列表
        PageResult<StoreResVO> pageResult = systemStoreInfoService.getStorePage(pageReqVO);
        List<StoreResVO> list = pageResult.getList();
        if (CollUtil.isEmpty(list)) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        List<YlbStoreConfigDO> configList = ylbStoreConfigService.list();
        Map<Long, YlbStoreConfigDO> configMap = configList.stream().collect(Collectors.toMap(YlbStoreConfigDO::getStoreId, r -> r, (old, newR) -> newR));

        list.forEach(item -> {
            YlbStoreConfigDO config = configMap.get(item.getStoreId());
            item.setReqState(config == null ? 1 : config.getReqState());
            item.setPushState(config == null ? 1 : config.getPushState());
        });

        pageResult.setList(list);
        return success(pageResult);
    }

    /**
     * 云喇叭开关切换
     */
    @PutMapping("/ylbConfig/update")
    @Operation(summary = "云喇叭开关切换")
    public CommonResult<String> ylbConfigReq(@Valid @RequestBody YlbConfigActionVO reqVO) {

        ylbStoreConfigService.remove(
                new LambdaQueryWrapper<YlbStoreConfigDO>()
                        .eq(YlbStoreConfigDO::getStoreId, reqVO.getStoreId())
        );

        YlbStoreConfigDO ylbStoreConfigDO = new YlbStoreConfigDO();
        ylbStoreConfigDO.setStoreId(reqVO.getStoreId());
        ylbStoreConfigDO.setReqState(reqVO.getReqState());
        ylbStoreConfigDO.setPushState(reqVO.getPushState());
        ylbStoreConfigService.save(ylbStoreConfigDO);

        Long storeId = reqVO.getStoreId();
        // 操作 Redis
        if (reqVO.getReqState() != null) {
            if (reqVO.getReqState() == 0) {
                stringRedisTemplate.opsForHash().put(REQ_HASH_KEY, storeId.toString(), "1");
            } else {
                stringRedisTemplate.opsForHash().delete(REQ_HASH_KEY, storeId.toString());
            }
        }

        if (reqVO.getPushState() != null) {
            if (reqVO.getPushState() == 0) {
                stringRedisTemplate.opsForHash().put(PUSH_HASH_KEY, storeId.toString(), "1");
            } else {
                stringRedisTemplate.opsForHash().delete(PUSH_HASH_KEY, storeId.toString());
            }
        }

        return CommonResult.success("OK");
    }

}
