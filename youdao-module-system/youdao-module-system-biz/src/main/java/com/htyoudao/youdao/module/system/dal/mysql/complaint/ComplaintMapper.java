package com.htyoudao.youdao.module.system.dal.mysql.complaint;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgPageReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.complaint.ComplaintDO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 投诉
 */
@Mapper
public interface ComplaintMapper extends BaseMapperX<ComplaintDO> {

    default PageResult<ComplaintDO> selectPage(ComplaintPageReqVO reqVO) {
        LambdaQueryWrapperX<ComplaintDO> queryWrapper = new LambdaQueryWrapperX<ComplaintDO>()
                .eqIfPresent(ComplaintDO::getComplaintType, reqVO.getStatus())
                .eqIfPresent(ComplaintDO::getBusinessType, reqVO.getType())
                .eqIfPresent(ComplaintDO::getMemberId, reqVO.getMemberId())
                .inIfPresent(ComplaintDO::getStoreId, reqVO.getOrgIds())
                .orderByDesc(ComplaintDO::getCreateTime);
        if (Boolean.TRUE.equals(reqVO.getIsBoss())&& reqVO.getStatus() == 1) {
            LocalDateTime halfYearAgo = LocalDateTime.now().minusMonths(6);
            queryWrapper.ge(ComplaintDO::getCreateTime, halfYearAgo);
        }
        return selectPage(reqVO, queryWrapper);
    }

}
