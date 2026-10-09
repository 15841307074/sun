package com.htyoudao.youdao.module.system.api.dict;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.dict.dto.DictDataRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "RPC 服务 - 字典数据")
public interface DictDataApi {


    @Operation(summary = "校验字典数据们是否有效")
    @Parameters({
        @Parameter(name = "dictType", description = "字典类型", example = "SEX", required = true),
        @Parameter(name = "descriptions", description = "字典数据值的数组", example = "1,2", required = true)
    })
    CommonResult<Boolean> validateDictDataList(@RequestParam("dictType") String dictType,
                                               @RequestParam("values") Collection<String> values);

    @Operation(summary = "获得指定的字典数据")
    @Parameters({
            @Parameter(name = "dictType", description = "字典类型", example = "SEX", required = true),
            @Parameter(name = "description", description = "字典数据值", example = "1", required = true)
    })
    CommonResult<DictDataRespDTO> getDictData(@RequestParam("dictType") String dictType,
                                              @RequestParam("value") String value);

    /**
     * 获得指定的字典标签，从缓存中
     *
     * @param type  字典类型
     * @param value 字典数据值
     * @return 字典标签
     */
    default String getDictDataLabel(String type, Integer value) {
        DictDataRespDTO dictData = getDictData(type, String.valueOf(value)).getData();
        if (ObjUtil.isNull(dictData)) {
            return StrUtil.EMPTY;
        }
        return dictData.getLabel();
    }

    @Operation(summary = "解析获得指定的字典数据")
    @Parameters({
            @Parameter(name = "dictType", description = "字典类型", example = "SEX", required = true),
            @Parameter(name = "label", description = "字典标签", example = "男", required = true)
    })
    CommonResult<DictDataRespDTO> parseDictData(@RequestParam("dictType") String dictType,
                                                @RequestParam("label") String label);

    @Operation(summary = "获得指定字典类型的字典数据列表")
    @Parameter(name = "dictType", description = "字典类型", example = "SEX", required = true)
    CommonResult<List<DictDataRespDTO>> getDictDataList(@RequestParam("dictType") String dictType);

    /**
     * 获得字典数据标签列表
     *
     * @param dictType 字典类型
     * @return 字典数据标签列表
     */
    default List<String> getDictDataLabelList(String dictType) {
        List<DictDataRespDTO> list = getDictDataList(dictType).getData();
        return convertList(list, DictDataRespDTO::getLabel);
    }

}
