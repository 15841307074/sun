package com.htyoudao.youdao.module.system.dal.mysql.dept;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptUserOAPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptUserOAReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Mapper
public interface DeptMapper extends BaseMapperX<DeptDO> {

    default List<DeptDO> selectList(DeptListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DeptDO>()
                .likeIfPresent(DeptDO::getName, reqVO.getName())
                .eqIfPresent(DeptDO::getStatus, reqVO.getStatus()));
    }

    default DeptDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(DeptDO::getParentId, parentId, DeptDO::getName, name);
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(DeptDO::getParentId, parentId);
    }

    default List<DeptDO> selectListByParentId(Collection<Long> parentIds) {
        return selectList(DeptDO::getParentId, parentIds);
    }
   @DataPermission(enable = false)
   default  DeptDO selectByDeptId(Long id){
       return selectById(id);
   }


    /**
     * 获取用户信息 OA用
     * @param page page
     * @param deptUserOAReqVO deptUserOAReqVO
     * @return Page<DeptUserOAPageRespVO>
     */
    Page<DeptUserOAPageRespVO> getUserAndDeptWithOAPage(@Param("page") Page<DeptUserOAPageRespVO> page,@Param("deptUserOAReqVO") DeptUserOAReqVO deptUserOAReqVO);

    /**
     * 获取用户信息 OA用
     * @param deptUserOAReqVO deptUserOAReqVO
     * @return Page<DeptUserOAPageRespVO>
     */
    List<DeptUserOAPageRespVO> getAllUserAndDeptPageWithOA(@Param("deptUserOAReqVO") DeptUserOAReqVO deptUserOAReqVO);
}
