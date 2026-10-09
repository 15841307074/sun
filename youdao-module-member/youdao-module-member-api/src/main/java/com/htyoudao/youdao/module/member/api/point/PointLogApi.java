package com.htyoudao.youdao.module.member.api.point;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.point.VO.ClientAddMemberPointReqVO;
import com.htyoudao.youdao.module.member.api.point.dto.PointsLogDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "积分日志")
public interface PointLogApi {
    /**
     * 添加积分记录
     */
    void addMemberPoint(@RequestBody ClientAddMemberPointReqVO reqVO);

    /**
     * 保存积分记录
     * @param pointsLogDTO pointsLogDTO
     * @return Boolean
     */
    CommonResult<Boolean> save(@RequestBody PointsLogDTO pointsLogDTO);
}

