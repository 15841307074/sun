package com.htyoudao.youdao.module.member.service.pointsLog;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.api.point.VO.ClientAddMemberPointReqVO;
import com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO.*;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.PointsLogVO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 积分记录Service接口
 *
 * @author Qizhongnan
 * @date 2024-02-03
 */
public interface IPointsLogByMemberService extends IService<PointsLogDO> {
    public List<PointsLogDO> selectPointsLogList(Long memberId);
}
