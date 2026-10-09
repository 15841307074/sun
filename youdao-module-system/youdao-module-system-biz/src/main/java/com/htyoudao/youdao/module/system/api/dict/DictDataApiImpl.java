package com.htyoudao.youdao.module.system.api.dict;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.dict.dto.DictDataRespDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.dict.DictDataDO;
import com.htyoudao.youdao.module.system.service.dict.DictDataService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
public class DictDataApiImpl implements DictDataApi {

    @Resource
    private DictDataService dictDataService;

    @Override
    public CommonResult<Boolean> validateDictDataList(String dictType, Collection<String> values) {
        dictDataService.validateDictDataList(dictType, values);
        return success(true);
    }

    @Override
    public CommonResult<DictDataRespDTO> getDictData(String dictType, String value) {
        DictDataDO dictData = dictDataService.getDictData(dictType, value);
        return success(BeanUtils.toBean(dictData, DictDataRespDTO.class));
    }

    @Override
    public CommonResult<DictDataRespDTO> parseDictData(String dictType, String label) {
        DictDataDO dictData = dictDataService.parseDictData(dictType, label);
        return success(BeanUtils.toBean(dictData, DictDataRespDTO.class));
    }

    @Override
    public CommonResult<List<DictDataRespDTO>> getDictDataList(String dictType) {
        List<DictDataDO> list = dictDataService.getDictDataListByDictType(dictType);
        return success(BeanUtils.toBean(list, DictDataRespDTO.class));
    }

}
