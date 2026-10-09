package com.htyoudao.youdao.module.system.service.wxtemplate.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplateDetailReqVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplatePageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplateReqVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.TemplateTagRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.wxstore.StoreWecomConfigDO;
import com.htyoudao.youdao.module.system.dal.dataobject.wxtemplate.StoreWecomTemplateDO;
import com.htyoudao.youdao.module.system.dal.dataobject.wxtemplateRelevance.StoreWecomRelevanceDO;
import com.htyoudao.youdao.module.system.dal.mysql.wxstore.StoreWecomConfigMapper;
import com.htyoudao.youdao.module.system.dal.mysql.wxtemplate.StoreWecomTemplateMapper;
import com.htyoudao.youdao.module.system.dal.mysql.wxtemplateRelevance.StoreWecomRelevanceMapper;
import com.htyoudao.youdao.module.system.enums.wxtemplate.*;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.service.taggroup.TagGroupService;
import com.htyoudao.youdao.module.system.service.wxstore.StoreWecomConfigService;
import com.htyoudao.youdao.module.system.service.wxtemplate.StoreWecomTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.ORG_USER_NOT_EXISTS;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.WX_NOT_DELETE;


@Slf4j
@Service
public class StoreWecomTemplateServiceImpl implements StoreWecomTemplateService {

    @Resource
    private StoreWecomTemplateMapper storeWecomTemplateMapper;

    @Resource
    private StoreWecomRelevanceMapper storeWecomRelevanceMapper;

    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    @Resource
    private TagGroupService tagGroupService;

    @Resource
    private StoreWecomConfigMapper storeWecomConfigMapper;


    @Override
    public Integer insertstoreWecomTemplate(StoreWecomTemplateReqVO storeWecomTemplateReqVO) {
        StoreWecomTemplateDO storeWecomTemplateDO = new StoreWecomTemplateDO();
        BeanUtils.copyProperties(storeWecomTemplateReqVO,storeWecomTemplateDO);
        storeWecomTemplateDO.setReleaseStatus(0);
        storeWecomTemplateDO.setState(1);
        storeWecomTemplateMapper.insert(storeWecomTemplateDO);
        if (ObjectUtil.isNotEmpty(storeWecomTemplateReqVO.getStoreList())){
            List<Long> storeList = storeWecomTemplateReqVO.getStoreList();
            //去重门店ID集合
            storeList = storeList.stream().distinct().toList();

            List<StoreWecomRelevanceDO> storeWecomRelevanceDOS = new ArrayList<>();
            for (Long storeId : storeList) {
                StoreWecomRelevanceDO storeWecomRelevanceDO = new StoreWecomRelevanceDO();
                storeWecomRelevanceDO.setTemplateId(storeWecomTemplateDO.getId());
                storeWecomRelevanceDO.setStoreId(storeId);
                storeWecomRelevanceDOS.add(storeWecomRelevanceDO);
            }
            storeWecomRelevanceMapper.insertBatch(storeWecomRelevanceDOS);

        }

        return 1;
    }

    @Override
    public Integer remove(long id) {
        StoreWecomTemplateDO storeWecomTemplateDO = storeWecomTemplateMapper.selectById(id);
        if(storeWecomTemplateDO!=null){
            Integer state = storeWecomTemplateDO.getState();
            if(state.equals(0)){
                throw exception(WX_NOT_DELETE);
            }
        }

        storeWecomTemplateMapper.deleteById(id);
        LambdaQueryWrapper<StoreWecomRelevanceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreWecomRelevanceDO::getTemplateId,id);
        storeWecomRelevanceMapper.delete(wrapper);
        return 1;
    }

    @Override
    public Integer updatestoreWecomTemplate(StoreWecomTemplateReqVO storeWecomTemplateReqVO) {
        Long id = storeWecomTemplateReqVO.getId();
        StoreWecomTemplateDO templateDO = storeWecomTemplateMapper.selectById(id);
        if(templateDO!=null){
            if(storeWecomTemplateReqVO.getApplicationScope().equals(1)){
                //证明有改变
                if(!storeWecomTemplateReqVO.getShowStore().equals(templateDO.getShowStore())){
                    //删除模版关联门店
                    LambdaQueryWrapper<StoreWecomRelevanceDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(StoreWecomRelevanceDO::getTemplateId,id);
                    storeWecomRelevanceMapper.delete(wrapper);
                    if(storeWecomTemplateReqVO.getShowStore().equals(2)){
                        if (ObjectUtil.isNotEmpty(storeWecomTemplateReqVO.getStoreList())) {
                            List<Long> storeList = storeWecomTemplateReqVO.getStoreList();
                            //去重门店ID集合
                            storeList = storeList.stream().distinct().toList();

                            List<StoreWecomRelevanceDO> storeWecomRelevanceDOS = new ArrayList<>();
                            for (Long storeId : storeList) {
                                StoreWecomRelevanceDO storeWecomRelevanceDO = new StoreWecomRelevanceDO();
                                storeWecomRelevanceDO.setTemplateId(id);
                                storeWecomRelevanceDO.setStoreId(storeId);
                                storeWecomRelevanceDOS.add(storeWecomRelevanceDO);
                            }
                            storeWecomRelevanceMapper.insertBatch(storeWecomRelevanceDOS);
                        }
                    }
                }else if(storeWecomTemplateReqVO.getShowStore().equals(2)){
                    LambdaQueryWrapper<StoreWecomRelevanceDO> relevanceDOLambdaQueryWrapper  = new LambdaQueryWrapper<>();
                    relevanceDOLambdaQueryWrapper.eq(StoreWecomRelevanceDO::getTemplateId,id);
                    List<StoreWecomRelevanceDO> storeWecomRelevanceDOS = storeWecomRelevanceMapper.selectList(relevanceDOLambdaQueryWrapper);
                    if(ObjectUtil.isNotEmpty(storeWecomRelevanceDOS)){
                        List<Long> collect = storeWecomRelevanceDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                        List<Long> storeIds = storeWecomTemplateReqVO.getStoreList();
                        // 使用HashSet判断是否相同
                        Set<Long> set1 = new HashSet<>(collect);
                        Set<Long> set2 = new HashSet<>(storeIds);

                        boolean isSame = set1.equals(set2);

                        if (isSame) {

                        } else {
                           // 获取需要删除的storeId（在collect中但不在storeIds中）
                            Set<Long> toDeleteStoreIds = new HashSet<>(set1);
                            toDeleteStoreIds.removeAll(set2);

                            // 获取需要新增的storeId（在storeIds中但不在collect中）
                            Set<Long> toAddStoreIds = new HashSet<>(set2);
                            toAddStoreIds.removeAll(set1);

                            log.info("需要删除的门店ID: {}", toDeleteStoreIds);
                            log.info("需要新增的门店ID: {}", toAddStoreIds);

                            // 执行相应的业务逻辑
                            if (!toDeleteStoreIds.isEmpty()) {
                                // 删除多余的门店
                                List<Long> longs = new ArrayList<>(toDeleteStoreIds);
                                LambdaQueryWrapper<StoreWecomRelevanceDO> storeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
                                storeDOLambdaQueryWrapper.eq(StoreWecomRelevanceDO::getTemplateId,id);
                                storeDOLambdaQueryWrapper.in(StoreWecomRelevanceDO::getStoreId,longs);
                                storeWecomRelevanceMapper.delete(storeDOLambdaQueryWrapper);

                            }

                            if (!toAddStoreIds.isEmpty()) {
                                // 新增缺少的门店
                                List<StoreWecomRelevanceDO> storeWecomRelevanceDOList = new ArrayList<>();
                                for (Long storeId : toAddStoreIds) {
                                    StoreWecomRelevanceDO storeWecomRelevanceDO = new StoreWecomRelevanceDO();
                                    storeWecomRelevanceDO.setTemplateId(id);
                                    storeWecomRelevanceDO.setStoreId(storeId);
                                    storeWecomRelevanceDOList.add(storeWecomRelevanceDO);
                                }
                                storeWecomRelevanceMapper.insertBatch(storeWecomRelevanceDOList);
                            }
                        }
                    }

                }
            }

        }
        LambdaUpdateWrapper<StoreWecomTemplateDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StoreWecomTemplateDO::getId, id)
                .set(StoreWecomTemplateDO::getTemplateName, storeWecomTemplateReqVO.getTemplateName())
                .set(StoreWecomTemplateDO::getWechatImage, storeWecomTemplateReqVO.getWechatImage())
                .set(StoreWecomTemplateDO::getGroupImage, storeWecomTemplateReqVO.getGroupImage())
                .set(StoreWecomTemplateDO::getApplicationScope, storeWecomTemplateReqVO.getApplicationScope())
                .set(StoreWecomTemplateDO::getShowStore, storeWecomTemplateReqVO.getShowStore())
                .set(StoreWecomTemplateDO::getTagNumbers, storeWecomTemplateReqVO.getTagNumbers());

        int result = storeWecomTemplateMapper.update(null, updateWrapper);
        return result;
    }

    @Override
    public StoreWecomTemplateDetailReqVO getByDetail(long id) {
        StoreWecomTemplateDetailReqVO storeWecomTemplateDetailReqVO = new StoreWecomTemplateDetailReqVO();
        StoreWecomTemplateDO templateDO = storeWecomTemplateMapper.selectById(id);
        if(templateDO!=null){
            BeanUtils.copyProperties(templateDO,storeWecomTemplateDetailReqVO);
            if (storeWecomTemplateDetailReqVO.getShowStore().equals(2)) {
                LambdaQueryWrapper<StoreWecomRelevanceDO> queryWrapper  = new LambdaQueryWrapper<>();
                queryWrapper.eq(StoreWecomRelevanceDO::getTemplateId,id);
                List<StoreWecomRelevanceDO> selectedList = storeWecomRelevanceMapper.selectList(queryWrapper);
                List<Long> storeIds = selectedList.stream().map(StoreWecomRelevanceDO::getStoreId).collect(Collectors.toList());
                List<StoreInfoDTO> storesByStoreIds = systemStoreInfoService.getStoresByStoreIds(storeIds);
                storeWecomTemplateDetailReqVO.setStoreInfoDTOS(storesByStoreIds);
            } else {
                storeWecomTemplateDetailReqVO.setStoreInfoDTOS(new ArrayList<>());
            }
                if(templateDO.getApplicationScope().equals(2)){
                    List<TemplateTagRespVO> tagNameList = new ArrayList<>();
                    List<Long> tagList = templateDO.getTagList();
                    if(ObjectUtil.isNotEmpty(tagList)){
                        for (Long aLong : tagList) {
                            TemplateTagRespVO templateTagRespVO = new TemplateTagRespVO();
                            String byTagName = tagGroupService.getByTagName(aLong);
                            if(byTagName!=null){
                                templateTagRespVO.setTagId(aLong);
                                templateTagRespVO.setTagName(byTagName);
                                tagNameList.add(templateTagRespVO);

                            }
                        }
                        storeWecomTemplateDetailReqVO.setTagNameList(tagNameList);
                    }

            }
        }
        return storeWecomTemplateDetailReqVO;
    }

    @Override
    public List<StoreWecomTemplatePageRespVO> getList() {
        List<StoreWecomTemplatePageRespVO> list = new ArrayList<>();
        LambdaQueryWrapper<StoreWecomTemplateDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .orderByAsc(StoreWecomTemplateDO::getState)
                .orderByDesc(StoreWecomTemplateDO::getCreateTime);
        List<StoreWecomTemplateDO> storeWecomTemplateDOS = storeWecomTemplateMapper.selectList(lambdaQueryWrapper);
        if(ObjectUtil.isNotEmpty(storeWecomTemplateDOS)){
            for (StoreWecomTemplateDO storeWecomTemplateDO : storeWecomTemplateDOS) {
                StoreWecomTemplatePageRespVO storeWecomTemplatePageRespVO = new StoreWecomTemplatePageRespVO();
                BeanUtils.copyProperties(storeWecomTemplateDO,storeWecomTemplatePageRespVO);
                if(storeWecomTemplateDO.getApplicationScope().equals(1)){
                    if (storeWecomTemplatePageRespVO.getShowStore().equals(2)) {
                        LambdaQueryWrapper<StoreWecomRelevanceDO> queryWrapper  = new LambdaQueryWrapper<>();
                        queryWrapper.eq(StoreWecomRelevanceDO::getTemplateId,storeWecomTemplateDO.getId());
                        List<StoreWecomRelevanceDO> selectedList = storeWecomRelevanceMapper.selectList(queryWrapper);
                        List<Long> storeIds = selectedList.stream().map(StoreWecomRelevanceDO::getStoreId).collect(Collectors.toList());
                        List<StoreInfoDTO> storesByStoreIds = systemStoreInfoService.getStoresByStoreIds(storeIds);
                        storeWecomTemplatePageRespVO.setStoreInfoDTOS(storesByStoreIds);
                    } else {
                        storeWecomTemplatePageRespVO.setStoreInfoDTOS(new ArrayList<>());
                    }
                }else{
                    //按标签
                    List<Long> tagList = storeWecomTemplateDO.getTagList();
                    if(ObjectUtil.isNotEmpty(tagList)){
                        List<StoreInfoDTO> storeInfoDTOS = systemStoreInfoService.selectAdvertisingStoreList(tagList);
                        if(ObjectUtil.isNotEmpty(storeInfoDTOS)){
                            storeWecomTemplatePageRespVO.setStoreInfoDTOS(storeInfoDTOS);
                        }
                    }
                }



                list.add(storeWecomTemplatePageRespVO);
            }
        }
        return list;
    }

    @Override
    public Integer isRelease(Long id, Integer status) {
        Date date = new Date();
        UpdateWrapper<StoreWecomTemplateDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("release_status", status);
        updateWrapper.set("release_time", date);
        int update = storeWecomTemplateMapper.update(updateWrapper);
        return update;
    }

    @Override
    public String getStoreIdByTemplate(Long storeId) {
        // 1. 获取所有已发布且启用的模板（按发布时间倒序）
        List<StoreWecomTemplateDO> templates = getPublishedEnabledTemplates();

        // 2. 优先匹配有效模板
        String result = matchTemplate(storeId, templates);
        if (result != null) {
            return result;
        }

        // 3. 没有匹配的模板，使用默认模板（state=0）
        return getDefaultTemplateResult(storeId);
    }

    /**
     * 获取已发布且启用的模板，按发布时间倒序
     */
    private List<StoreWecomTemplateDO> getPublishedEnabledTemplates() {
        LambdaQueryWrapper<StoreWecomTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreWecomTemplateDO::getReleaseStatus, ReleaseStatus.PUBLISHED.getCode())
                .eq(StoreWecomTemplateDO::getState, StateEnum.ENABLED.getCode())
                .orderByDesc(StoreWecomTemplateDO::getReleaseTime);
        return storeWecomTemplateMapper.selectList(wrapper);
    }

    /**
     * 匹配模板
     */
    private String matchTemplate(Long storeId, List<StoreWecomTemplateDO> templates) {
        if (ObjectUtil.isEmpty(templates)) {
            return null;
        }

        for (StoreWecomTemplateDO template : templates) {
            String imageUrl = checkAndGetTemplateImage(storeId, template);
            if (imageUrl != null) {
                return imageUrl;
            }
        }
        return null;
    }

    /**
     * 检查模板是否匹配并返回对应的二维码图片
     */
    private String checkAndGetTemplateImage(Long storeId, StoreWecomTemplateDO template) {
        boolean isMatch = false;

        // 根据应用范围判断是否匹配
        if (ApplicationScope.ALL_STORES.getCode().equals(template.getApplicationScope())) {
            isMatch = checkAllStoresMatch(storeId, template);
        } else if (ApplicationScope.BY_TAG.getCode().equals(template.getApplicationScope())) {
            isMatch = checkByTagMatch(storeId, template);
        }

        // 如果匹配，返回对应的二维码图片
        if (isMatch) {
            return getQrImageByStoreConfig(storeId, template);
        }
        return null;
    }

    /**
     * 全部门店匹配逻辑
     */
    private boolean checkAllStoresMatch(Long storeId, StoreWecomTemplateDO template) {
        if (ShowStoreEnum.ALL.getCode().equals(template.getShowStore())) {
            return true;
        } else if (ShowStoreEnum.PARTIAL.getCode().equals(template.getShowStore())) {
            return isStoreInTemplate(storeId, template.getId());
        }
        return false;
    }

    /**
     * 按标签匹配逻辑
     */
    private boolean checkByTagMatch(Long storeId, StoreWecomTemplateDO template) {
        List<Long> tagList = template.getTagList();
        if (ObjectUtil.isEmpty(tagList)) {
            return false;
        }

        // 只要有一个标签匹配即可
        return tagList.stream()
                .anyMatch(tag -> systemStoreInfoService.isTag(storeId, tag));
    }

    /**
     * 判断门店是否在模板的部分门店列表中
     */
    private boolean isStoreInTemplate(Long storeId, Long templateId) {
        LambdaQueryWrapper<StoreWecomRelevanceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreWecomRelevanceDO::getTemplateId, templateId)
                .eq(StoreWecomRelevanceDO::getStoreId, storeId);
        return ObjectUtil.isNotEmpty(storeWecomRelevanceMapper.selectList(wrapper));
    }

    /**
     * 根据门店配置获取二维码图片
     */
    private String getQrImageByStoreConfig(Long storeId, StoreWecomTemplateDO template) {
        StoreWecomConfigDO config = getStoreWecomConfig(storeId);
        QrTypeEnum qrType = config != null ?
                QrTypeEnum.fromCode(config.getQrType()) : QrTypeEnum.WECHAT;

        return QrTypeEnum.WECHAT.equals(qrType) ?
                template.getWechatImage() : template.getGroupImage();
    }

    /**
     * 获取门店的企业微信配置
     */
    private StoreWecomConfigDO getStoreWecomConfig(Long storeId) {
        LambdaQueryWrapper<StoreWecomConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreWecomConfigDO::getStoreId, storeId);
        List<StoreWecomConfigDO> configs = storeWecomConfigMapper.selectList(wrapper);
        return ObjectUtil.isNotEmpty(configs) ? configs.get(0) : null;
    }

    /**
     * 获取默认模板结果
     */
    private String getDefaultTemplateResult(Long storeId) {
        List<StoreWecomTemplateDO> defaultTemplates = getDefaultTemplates();
        if (ObjectUtil.isEmpty(defaultTemplates)) {
            return null;
        }

        StoreWecomTemplateDO defaultTemplate = defaultTemplates.get(0);
        return checkAndGetTemplateImage(storeId, defaultTemplate);
    }

    /**
     * 获取默认模板（state=0）
     */
    private List<StoreWecomTemplateDO> getDefaultTemplates() {
        LambdaQueryWrapper<StoreWecomTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreWecomTemplateDO::getState, StateEnum.DISABLED.getCode());
        return storeWecomTemplateMapper.selectList(wrapper);
    }
}
