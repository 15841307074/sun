package com.htyoudao.youdao.module.bpm.dal.mysql.definition;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.form.BpmFormPageReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmFormDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态表单 Mapper
 *
 * @author 风里雾里
 */
@Mapper
public interface BpmFormMapper extends BaseMapperX<BpmFormDO> {

    default PageResult<BpmFormDO> selectPage(BpmFormPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<BpmFormDO>()
                .likeIfPresent("name", reqVO.getName())
                .orderByDesc("id"));
    }

}
