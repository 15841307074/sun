package com.htyoudao.youdao.module.system.dal.mysql.dept;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.AppDeptUserRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.UserDeptDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author dht
 */
@Mapper
public interface UserDeptMapper extends BaseMapperX<UserDeptDO> {

    /**
     * 获取所有和部门有关系的用户
     * @return List
     */
    List<AppDeptUserRespVO> getAllDeptAndUserList();
}
