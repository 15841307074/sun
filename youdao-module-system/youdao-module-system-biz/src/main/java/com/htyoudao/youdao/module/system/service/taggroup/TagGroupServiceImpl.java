package com.htyoudao.youdao.module.system.service.taggroup;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.taggroup.TagGroupDO;
import com.htyoudao.youdao.module.system.dal.dataobject.tagvalue.TagValueDO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreTagMapper;
import com.htyoudao.youdao.module.system.dal.mysql.taggroup.TagGroupMapper;
import com.htyoudao.youdao.module.system.dal.mysql.tagvalue.TagValueMapper;
import com.htyoudao.youdao.module.system.service.applet.AppletPageManagementService;
import com.htyoudao.youdao.module.system.service.tagvalue.TagValueService;
import com.xingyuv.http.util.StringUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;

/**
 * 标签组 Service 实现类
 *
 * @author dht
 */
@Service
@Validated
public class TagGroupServiceImpl implements TagGroupService {

    @Resource
    private TagGroupMapper tagGroupMapper;

    @Resource
    private TagValueService tagValueService;

    @Resource
    private TagValueMapper tagValueMapper;

    @Resource
    private SystemStoreTagMapper systemStoreTagMapper;

    @Resource
    private AppletPageManagementService appletPageManagementService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTagGroup(TagGroupSaveReqVO createReqVO) {
        // 校验重复
        createVerifyTagGroupDuplication(createReqVO.getName());
        // 插入
        TagGroupDO tagGroup = BeanUtils.toBean(createReqVO, TagGroupDO.class);
        tagGroup.setId(null);
        tagGroupMapper.insert(tagGroup);
        Long id = tagGroup.getId();
        // 插入标签值
        List<TagValueSaveReqVO> tagValueList = createReqVO.getTagValueList();
        tagValueList.forEach(tagValue -> {
            tagValue.setTagGroupId(id);
        } );
        tagValueService.insertBatch(tagValueList);
        // 返回
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTagGroup(TagGroupUpdateReqVO updateReqVO) {
        // 校验存在
        validateTagGroupExists(updateReqVO.getId());
        // 更新校验重复
        updateVerifyTagGroupDuplication(updateReqVO);

        // 更新
        TagGroupDO updateObj = BeanUtils.toBean(updateReqVO, TagGroupDO.class);
        tagGroupMapper.updateById(updateObj);
        // 删除标签值
        List<Long> tagValueIdList = updateReqVO.getTagValueIdList();
        if (CollectionUtil.isNotEmpty(tagValueIdList)) {
            QueryWrapper<SystemStoreTagDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("tag_id", tagValueIdList);
            Long l = systemStoreTagMapper.selectCount(queryWrapper);
            if (l > 0) {
                throw exception(TAG_STORE_VALUE_EXISTS);
            }
            tagValueService.deleteBatch(tagValueIdList);

            // 删除页面装修中这个标签的配置
            appletPageManagementService.delAppletPageManagementTags(tagValueIdList);
        }
        // 更新标签值
        List<TagValueSaveReqVO> updateValueList = updateReqVO.getUpdateValueList();
        if(CollectionUtil.isNotEmpty(updateValueList)){
            tagValueService.updateBatch(updateValueList);
        }
        // 新增标签值
        List<TagValueSaveReqVO> tagValueList = updateReqVO.getTagValueList();
        if (CollectionUtil.isNotEmpty(tagValueList)) {
            tagValueList.forEach(tagValue -> {
                tagValue.setTagGroupId(updateReqVO.getId());
            });
            tagValueService.insertBatch(tagValueList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTagGroup(Long id) {
        // 校验存在
        validateTagGroupExists(id);
        // 删除标签组
        tagGroupMapper.deleteById(id);
        List<TagValueRespVO> listByGroupId = tagValueService.getListByGroupId(id);
        if (CollectionUtil.isNotEmpty(listByGroupId)) {
            List<Long> tagValueIdList = listByGroupId.stream().map(TagValueRespVO::getId).toList();
            QueryWrapper<SystemStoreTagDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("tag_id", tagValueIdList);
            Long l = systemStoreTagMapper.selectCount(queryWrapper);
            if (l > 0) {
                throw exception(TAG_STORE_VALUE_EXISTS);
            }
        }
        // 删除标签值
        tagValueService.deleteByTagGroupId(id);
    }

    /**
     * 校验标签组是否存在
     * @param id 标签组id
     */
    private void validateTagGroupExists(Long id) {
        if (tagGroupMapper.selectById(id) == null) {
            throw exception(TAG_GROUP_NOT_EXISTS);
        }
    }

    /**
     * 校验标签组是否重复(create时)
     *
     * @param name 标签组名称
     */
    private void createVerifyTagGroupDuplication(String name) {
        LambdaQueryWrapper<TagGroupDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TagGroupDO::getName, name);
        long count = tagGroupMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw exception(TAG_GROUP_EXISTS);
        }
    }

    /**
     * 校验标签组是否重复(update时)
     *
     * @param updateReqVO 标签组实体
     */
    private void updateVerifyTagGroupDuplication(TagGroupUpdateReqVO updateReqVO) {
        LambdaQueryWrapper<TagGroupDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TagGroupDO::getName, updateReqVO.getName());
        queryWrapper.ne(TagGroupDO::getId, updateReqVO.getId());
        long count = tagGroupMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw exception(TAG_GROUP_EXISTS);
        }
    }


    @Override
    public TagGroupRespVO getTagGroup(Long id) {
        TagGroupDO tagGroupDO = tagGroupMapper.selectById(id);
        TagGroupRespVO tagGroupRespVO = BeanUtils.toBean(tagGroupDO, TagGroupRespVO.class);
        List<TagValueRespVO> tagValues = tagValueService.getListByGroupId(id);
        tagGroupRespVO.setTagValues(tagValues);
        return tagGroupRespVO;
    }

    @Override
    public PageResult<TagGroupRespVO> getTagGroupPage(TagGroupPageReqVO pageReqVO) {
        PageResult<TagGroupDO> result;
        if(ObjectUtil.isNotEmpty(pageReqVO.getName())){
            List<Long> ids = tagValueService.getTagGroupIdByValueName(pageReqVO.getName());
            QueryWrapper<TagGroupDO> queryWrapper = new QueryWrapper<>();
            if (CollectionUtil.isNotEmpty(ids)) {
                queryWrapper.in("id", ids).or().like("name", pageReqVO.getName());
            }else {
                queryWrapper.like("name", pageReqVO.getName());
            }
            result = tagGroupMapper.selectPage(pageReqVO, queryWrapper);
            PageResult<TagGroupRespVO> pageResult = BeanUtils.toBean(result, TagGroupRespVO.class);
            List<TagGroupRespVO> list = pageResult.getList();
            // 设置标签值
            groupListSetValues(list,pageReqVO.getName());
            return pageResult;
        }
        result = tagGroupMapper.selectPage(pageReqVO);
        // 转换
        PageResult<TagGroupRespVO> pageResult = BeanUtils.toBean(result, TagGroupRespVO.class);
        List<TagGroupRespVO> list = pageResult.getList();
        // 设置标签值
        groupListSetValues(list,null);
        return pageResult;
    }

    @Override
    public List<TagGroupRespVO> getTagGroupByName(String tagName) {
        List<TagGroupRespVO> tagGroupResList = new ArrayList<>();

        if (StringUtil.isNotEmpty(tagName)) {
            List<TagValueDO> tagValueList = tagValueService.selectListByTagName(tagName);
            Map<Long, List<TagValueDO>> tagValueMap = tagValueList.stream().collect(Collectors.groupingBy(TagValueDO::getTagGroupId));
            if (CollectionUtil.isNotEmpty(tagValueList)) {
                List<Long> tagGroupIds = tagValueList.stream().map(TagValueDO::getTagGroupId).toList();
                List<TagGroupDO> tagGroupList = tagGroupMapper.selectBatchIds(tagGroupIds);
                tagGroupResList = BeanUtils.toBean(tagGroupList, TagGroupRespVO.class);
                tagGroupResList.forEach(tagGroup -> {
                    List<TagValueDO> tagValues = tagValueMap.get(tagGroup.getId());
                    List<TagValueRespVO> respVOList =BeanUtils.toBean(tagValues, TagValueRespVO.class);
                    tagGroup.setTagValues(respVOList);
                });
                return tagGroupResList;
            } else {
                return new ArrayList<>();
            }
        }
        List<TagGroupDO> result = tagGroupMapper.selectList();
        // 转换
        tagGroupResList = BeanUtils.toBean(result, TagGroupRespVO.class);
        if (CollectionUtil.isNotEmpty(tagGroupResList)) {
            // 根据标签组id查询标签值
            List<Long> groupIds = tagGroupResList.stream().map(TagGroupRespVO::getId).toList();
            List<TagValueRespVO> tagValues = tagValueService.getListByGroupIds(groupIds,null);
            Map<Long, List<TagValueRespVO>> groupIdTagValueMap = tagValues.stream()
                    .collect(Collectors.groupingBy(TagValueRespVO::getTagGroupId));
            // 设置标签值
            for (TagGroupRespVO tagGroupRespVO : tagGroupResList) {
                List<TagValueRespVO> tagValueResp = groupIdTagValueMap.get(tagGroupRespVO.getId());
                if (CollectionUtil.isNotEmpty(tagValueResp)) {
                    tagGroupRespVO.setTagValues(tagValueResp);
                }
            }
        }
        return tagGroupResList;
    }
  @Override
  public List<TagValueRespVO> getTagList() {
        List<TagValueDO> tagValueList = tagValueService.selectListByTagName(null);
        List<TagValueRespVO> tagValueRespVOList = BeanUtils.toBean(tagValueList, TagValueRespVO.class);
        return tagValueRespVOList;
    }

    @Override
    public List<TagGroupRespVO> tagList() {
        List<TagGroupRespVO> list = new ArrayList<>();
//        if(ObjectUtil.isNotEmpty(pageReqVO.getName())){
//            List<Long> ids = tagValueService.getTagGroupIdByValueName(pageReqVO.getName());
//            QueryWrapper<TagGroupDO> queryWrapper = new QueryWrapper<>();
//            if (CollectionUtil.isNotEmpty(ids)) {
//                queryWrapper.in("id", ids).or().like("name", pageReqVO.getName());
//            }else {
//                queryWrapper.like("name", pageReqVO.getName());
//            }
//            result = tagGroupMapper.selectPage(pageReqVO, queryWrapper);
//            PageResult<TagGroupRespVO> pageResult = BeanUtils.toBean(result, TagGroupRespVO.class);
//            List<TagGroupRespVO> list = pageResult.getList();
//            // 设置标签值
//            groupListSetValues(list,pageReqVO.getName());
//            return pageResult;
//        }
        List<TagGroupDO> tagGroupDOS = tagGroupMapper.selectList(null);
        // 转换
//        PageResult<TagGroupRespVO> pageResult = BeanUtils.toBean(result, TagGroupRespVO.class);
//        List<TagGroupRespVO> list = pageResult.getList();
        list = BeanCopyUtils.copyBeanList(tagGroupDOS, TagGroupRespVO.class);
        // 设置标签值
        groupListSetValues(list,null);
        return list;
    }

    @Override
    public String getByTagName(Long tagId) {
        LambdaQueryWrapper<TagValueDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagValueDO::getId,tagId);
        TagValueDO tagValueDO = tagValueMapper.selectOne(wrapper);
        if(tagValueDO!=null){
            return tagValueDO.getName();
        }else{
            return null;
        }
    }

    /**
     * 标签组集合设置标签值
     * @param list 标签组集合
     */
    private void groupListSetValues(List<TagGroupRespVO> list,String name){
        if (CollectionUtil.isNotEmpty(list)) {
            // 根据标签组id查询标签值
            List<Long> groupIds = list.stream().map(TagGroupRespVO::getId).toList();
            List<TagValueRespVO> tagValues = tagValueService.getListByGroupIds(groupIds,name);
            Map<Long, List<TagValueRespVO>> groupIdTagValueMap = tagValues.stream()
                    .collect(Collectors.groupingBy(TagValueRespVO::getTagGroupId));
            // 设置标签值
            for (TagGroupRespVO tagGroupRespVO : list) {
                List<TagValueRespVO> tagValueResp = groupIdTagValueMap.get(tagGroupRespVO.getId());
                if (CollectionUtil.isNotEmpty(tagValueResp)) {
                    tagGroupRespVO.setTagValues(tagValueResp);
                }
            }
        }
    }



}