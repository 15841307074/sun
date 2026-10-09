package com.htyoudao.youdao.module.commodity.service.materialLog;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO.MaterialLogReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO.MaterialLogRespVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLogDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLossRecordDO;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialLogMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialLossRecordMapper;
import com.htyoudao.youdao.module.commodity.enums.RawMaterialLog;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.api.storeinfo.StoreInfoApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.POINTS_PAGE_1000_NOT_E;

@Service
public class RawMaterialLogServiceImpl extends ServiceImpl<RawMaterialLogMapper, RawMaterialLogDO> implements RawMaterialLogService {

    @Autowired
    private RawMaterialLogMapper logMapper;

    @DubboReference
    private OrgStoreApi orgStoreApi;

    @DubboReference
    private StoreInfoApi storeInfoApi;


    @DubboReference
    private StoreApi storeApi;

    @Async("logTaskExecutor")
    public void asyncSaveLog(RawMaterialLogDO log) {

        logMapper.insert(log);
    }

    @Override
    public PageResult<MaterialLogRespVo> logList(MaterialLogReqVo pageReq) {
        if(pageReq.getPageNo() > 1000){
            throw exception(POINTS_PAGE_1000_NOT_E);
        }
        PageResult<MaterialLogRespVo> pageResult = new PageResult<>();
        LambdaQueryWrapperX<RawMaterialLogDO> wrapper = new LambdaQueryWrapperX<>();
        if(ObjectUtil.isEmpty(pageReq.getOrgId())){
            wrapper.eqIfPresent(RawMaterialLogDO::getStoreId,pageReq.getStoreId());
        }
        if(ObjectUtil.isNotEmpty(pageReq.getOrgId())){
            CommonResult<List<Long>> listCommonResult = orgStoreApi.selectByOrgStoreList(pageReq.getOrgId());
            if (!StringUtils.isEmpty(listCommonResult.getData())) {
                List<Long> data = listCommonResult.getData();
                if (data.size() > 0) {
                    wrapper.in(RawMaterialLogDO::getStoreId,data);
                }else{
                    pageResult.setTotal(0L);
                    pageResult.setList(new ArrayList<>());
                    return pageResult;
                }
            }else{
                pageResult.setTotal(0L);
                pageResult.setList(new ArrayList<>());
                return pageResult;

            }

        }
        wrapper.eq(RawMaterialLogDO::getStatus,0);
        wrapper.orderByDesc(RawMaterialLogDO::getCreateTime);
//        wrapper.in(RawMaterialLogDO::getLogType,2,3);
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageReq.getPageNo());
        pageParam.setPageSize(pageReq.getPageSize());
        PageResult<RawMaterialLogDO> rawMaterialLogDOPageResult = logMapper.selectPage(pageParam, wrapper);
        List<RawMaterialLogDO> list = rawMaterialLogDOPageResult.getList();
        if(ObjectUtil.isNotEmpty(list)){
            List<MaterialLogRespVo> materialLogRespVos = new ArrayList<>();
            for (RawMaterialLogDO rawMaterialLogDO : list) {
                MaterialLogRespVo materialLogRespVo = new MaterialLogRespVo();
                BeanUtils.copyProperties(rawMaterialLogDO, materialLogRespVo);
                CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(rawMaterialLogDO.getStoreId());
                StoreDTO data = storeByStoreId.getData();
                if(ObjectUtil.isNotEmpty(data)){
                    materialLogRespVo.setStoreName(data.getStoreName());
                }
                materialLogRespVos.add(materialLogRespVo);


            }

            pageResult.setTotal(rawMaterialLogDOPageResult.getTotal());
            pageResult.setList(materialLogRespVos);
            return pageResult;
        }else{
            pageResult.setList(new ArrayList<>());
            pageResult.setTotal(0L);
            return pageResult;
        }
    }
}
