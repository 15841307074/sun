package com.htyoudao.youdao.module.member.api.point;

import cn.hutool.core.bean.BeanUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.point.VO.ClientAddMemberPointReqVO;
import com.htyoudao.youdao.module.member.api.point.dto.PointsLogDTO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.service.pointsLog.IPointsLogService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;


@DubboService
public class PointLogApiImpl implements PointLogApi {

    @Resource
    private IPointsLogService pointsLogService;

    @Override
    public void addMemberPoint(ClientAddMemberPointReqVO reqVO) {
        pointsLogService.addMemberPoint(reqVO);
    }

    @Override
    public CommonResult<Boolean> save(PointsLogDTO pointsLogDTO) {
        PointsLogDO bean = BeanUtil.toBean(pointsLogDTO, PointsLogDO.class);
        return CommonResult.success(pointsLogService.save(bean));
    }


}
