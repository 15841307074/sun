package com.htyoudao.youdao.module.promotion.service.activityChannelName;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelNameDataRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo.ActivityChannelNamePageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo.ActivityChannelNameSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannelName.ActivityChannelNameDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public interface ActivityChannelNameService {

    Long create(@Valid ActivityChannelNameSaveReqVO reqVO);

    void update(@Valid ActivityChannelNameSaveReqVO reqVO);

    void updateStatus(@NotNull(message = "id不能为空") Long id,
                      @NotNull(message = "是否启用不能为空") Integer isEnable);

    void delete(@NotNull(message = "id不能为空") Long id);

    ActivityChannelNameDO get(@NotNull(message = "id不能为空") Long id);

    PageResult<ActivityChannelNameDO> page(@Valid ActivityChannelNamePageReqVO reqVO);

    List<ActivityChannelNameDataRespVO> getChannelList();

    /**
     * 获取渠道名称
     * @return Map
     */
    Map<Long, String> selectChannelNameMap();
}

