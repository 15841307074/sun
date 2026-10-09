package com.htyoudao.youdao.module.system.service.store;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.SysStoreExtendReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.SysStoreExtendResVO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SysStoreExtendDO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SysStoreExtendMapper;
import com.htyoudao.youdao.module.system.service.business.BusinessService;
import com.htyoudao.youdao.module.system.util.page.PageUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.MER_DUPLICATE_RECORD;

/**
 * <p>
 * 店铺关系表 服务实现类
 * </p>
 *
 * @author zhangjihe
 * @since 2024-05-08
 */
@Service
public class SysStoreExtendServiceImpl extends ServiceImpl<SysStoreExtendMapper, SysStoreExtendDO> implements SysStoreExtendService {

    @Resource
    private BusinessService businessService;

    @Override
    public Page<SysStoreExtendResVO> listPage(SysStoreExtendReqVO reqVO) {

        Page<SysStoreExtendResVO> returnPage = new Page<>();

        LambdaQueryWrapper<SysStoreExtendDO> extendLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotEmpty(reqVO.getStoreId())) {
            extendLambdaQueryWrapper.eq(SysStoreExtendDO::getStoreId, reqVO.getStoreId());
        }
        if (ObjectUtil.isNotEmpty(reqVO.getCode())) {
            extendLambdaQueryWrapper.like(SysStoreExtendDO::getCode, reqVO.getCode());
        }
        if (ObjectUtil.isNotEmpty(reqVO.getTerminalSn())) {
            extendLambdaQueryWrapper.eq(SysStoreExtendDO::getTerminalSn, reqVO.getTerminalSn());
        }
        if (ObjectUtil.isNotEmpty(reqVO.getTerminalKey())) {
            extendLambdaQueryWrapper.eq(SysStoreExtendDO::getTerminalKey, reqVO.getTerminalKey());
        }
        Page<SysStoreExtendDO> pageInfo = PageUtils.getPageInfo();
        pageInfo.setCurrent(reqVO.getPageNo());
        pageInfo.setSize(reqVO.getPageSize());

        Page<SysStoreExtendDO> sysStoreExtendDOPage = baseMapper.selectPage(pageInfo, extendLambdaQueryWrapper);
        List<SysStoreExtendDO> records = sysStoreExtendDOPage.getRecords();

        List<SysStoreExtendResVO> sysStoreExtendResVOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)) {
            sysStoreExtendResVOS = BeanCopyUtils.copyBeanList(records, SysStoreExtendResVO.class);
        }

        BeanUtils.copyProperties(sysStoreExtendDOPage, returnPage);
        returnPage.setRecords(sysStoreExtendResVOS);

        return returnPage;
    }

    @Override
    public void createOrUpdate(SysStoreExtendReqVO reqVO) {
//        String loginUsername = SecurityFrameworkUtils.getLoginUsername();
//        if (!Objects.equals(loginUsername, "13381170480")) {
//            throw exception(FORBIDDEN);
//        }

        List<BusinessDTO> businessDTOS = businessService.listAll();
        if (CollectionUtils.isEmpty(businessDTOS)) {
            return;
        }

        if (ObjectUtil.isEmpty(reqVO.getExtendId())) {

            Map<Long, String> businessMap = businessDTOS.stream().collect(Collectors.toMap(BusinessDTO::getId, BusinessDTO::getName, (key1, key2) -> key1));
            reqVO.setCode(
                    reqVO.getCode() + "【" + businessMap.get(reqVO.getBusinessId()) + "】"
            );

            LambdaQueryWrapper<SysStoreExtendDO> sysStoreExtendLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysStoreExtendLambdaQueryWrapper.eq(SysStoreExtendDO::getStoreId, reqVO.getStoreId());
            List<SysStoreExtendDO> list = this.list(sysStoreExtendLambdaQueryWrapper);
            if (ObjectUtil.isNotEmpty(list)) {
                throw exception(MER_DUPLICATE_RECORD);
            }

            SysStoreExtendDO sysStoreExtend = BeanCopyUtils.copyBean(reqVO, SysStoreExtendDO.class);
            this.save(sysStoreExtend);
        } else {
            LambdaUpdateWrapper<SysStoreExtendDO> sysStoreExtendLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            sysStoreExtendLambdaUpdateWrapper.eq(SysStoreExtendDO::getExtendId, reqVO.getExtendId());
            sysStoreExtendLambdaUpdateWrapper.eq(SysStoreExtendDO::getStoreId, reqVO.getStoreId());
            sysStoreExtendLambdaUpdateWrapper.set(SysStoreExtendDO::getTerminalSn, reqVO.getTerminalSn());
            sysStoreExtendLambdaUpdateWrapper.set(SysStoreExtendDO::getTerminalKey, reqVO.getTerminalKey());
            this.update(sysStoreExtendLambdaUpdateWrapper);
        }
    }

    @Override
    public void matchingField() {
        baseMapper.matchingField();
    }
}
