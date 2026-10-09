package com.htyoudao.youdao.module.system.api.dept;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptDTO;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import com.htyoudao.youdao.module.system.api.dept.dto.StoreRespDTO;
import com.htyoudao.youdao.module.system.api.dept.dto.UserDeptInfoRespDTO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptRespVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreResVO;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.DeptUserRespVO;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.UserDeptInfoRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.UserDeptDO;
import com.htyoudao.youdao.module.system.dal.mysql.dept.UserDeptMapper;
import com.htyoudao.youdao.module.system.enums.dept.UserDeptConstants;
import com.htyoudao.youdao.module.system.service.dept.DeptService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class DeptApiImpl implements DeptApi {

    @Resource
    private DeptService deptService;

    @Resource
    private UserDeptMapper userDeptMapper;

    @Override
    public CommonResult<DeptRespDTO> getDept(Long id) {
        DeptDO dept = deptService.getDept(id);
        DeptRespDTO bean = getDeptRespDTO(id, dept);
        return success(bean);
    }

    private DeptRespDTO getDeptRespDTO(Long id, DeptDO dept) {
        if (dept == null) {
            return null;
        }

        LambdaQueryWrapper<UserDeptDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDeptDO::getDeptId, id);
        queryWrapper.eq(UserDeptDO::getType, 0);
        UserDeptDO userDeptDO = userDeptMapper.selectOne(queryWrapper);

        DeptRespDTO bean = BeanUtils.toBean(dept, DeptRespDTO.class);

        if (bean != null && userDeptDO != null) {
            bean.setLeaderUserId(userDeptDO.getUserId());
        }
        return bean;
    }

    @Override
    public CommonResult<Map<Long,DeptRespDTO>> getDeptListByUserIds(Collection<Long> ids) {
        Map<Long, DeptDO> deptListByUserIds = deptService.getDeptListByUserIds(ids);
        Map<Long,DeptRespDTO> result = new HashMap<>(8);
        deptListByUserIds.forEach((id, dept) -> result.put(id, BeanUtils.toBean(dept, DeptRespDTO.class)));
        return success(result);
    }

    @Override
    public CommonResult<Boolean> validateDeptList(Collection<Long> ids) {
        //deptService.validateDeptList(ids);
        return success(true);
    }

    @Override
    public CommonResult<List<DeptRespDTO>> getChildDeptList(Long id) {
        //List<DeptDO> depts = deptService.getChildDeptList(id);
        //return success(BeanUtils.toBean(depts, DeptRespDTO.class));
        return success(null);
    }

    @Override
    public CommonResult<List<DeptRespDTO>> getDeptList(Set<Long> deptIds) {
        List<DeptDO> deptList = deptService.getDeptList(deptIds);
        if (CollectionUtils.isEmpty(deptList)){
            return success(List.of());
        }
        List<DeptRespDTO> respDTOS = new ArrayList<>();
        for (DeptDO deptDO : deptList) {
            DeptRespDTO deptRespDTO = getDeptRespDTO(deptDO.getId(), deptDO);
            respDTOS.add(deptRespDTO);
        }
        return success(respDTOS);
    }

    @Override
    public CommonResult<Long> getDeptManager(Long deptId) {
        DeptUserRespVO deptUserRespVO = deptService.getDeptManager(deptId);
        return success(deptUserRespVO.getId());
    }

    @Override
    public CommonResult<List<Long>> getManagerDeptByUserId(Long userId) {
        LambdaQueryWrapperX<UserDeptDO> queryWrapperX = new LambdaQueryWrapperX();
        queryWrapperX.eq(UserDeptDO::getUserId, userId);
        queryWrapperX.eq(UserDeptDO::getType, UserDeptConstants.USER_DEPT_TYPE_0);
        List<UserDeptDO> userDeptDOS = userDeptMapper.selectList(queryWrapperX);
        if (CollectionUtils.isEmpty(userDeptDOS)){
            return CommonResult.success(null);
        }

        List<Long> manageDeptIds = new ArrayList<>();

        List<Long> deptIds = userDeptDOS.stream().map(UserDeptDO::getDeptId).distinct().toList();
        List<DeptDO> childDeptList = deptService.getChildDeptList(deptIds);

        manageDeptIds.addAll(deptIds);
        manageDeptIds.addAll(childDeptList.stream().map(DeptDO::getId).toList());

        return CommonResult.success(manageDeptIds);
    }

    @Override
    public CommonResult<UserDeptInfoRespDTO> getUserDeptInfo(Long userId) {
        UserDeptInfoRespVO userDeptInfo = deptService.getUserDeptInfo(userId);
        UserDeptInfoRespDTO userDeptInfoRespDTO = BeanUtils.toBean(userDeptInfo, UserDeptInfoRespDTO.class);
        List<DeptRespVO> depts = userDeptInfo.getDepts();
        userDeptInfoRespDTO.setDepts(BeanUtils.toBean(depts, DeptRespDTO.class));
        List<StoreResVO> stores = userDeptInfo.getStores();
        userDeptInfoRespDTO.setStores(BeanUtils.toBean(stores, StoreRespDTO.class));
        return success(userDeptInfoRespDTO);
    }

    @Override
    public CommonResult<Map<Long, DeptDTO>> getParentDeptList(List<Long> ids) {
        return success(deptService.getParentDeptList(ids));
    }

    @Override
    public CommonResult<List<Long>> getSonDeptList(Long id) {
        return success(deptService.getSonDeptList(id));
    }
}
