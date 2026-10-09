package com.htyoudao.youdao.module.member.service.tagvalue;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.member.controller.admin.tag.vo.MemberTagVO;
import com.htyoudao.youdao.module.member.controller.admin.tag.vo.TagValuePageReqVO;
import com.htyoudao.youdao.module.member.controller.admin.tag.vo.TagValueRespVO;
import com.htyoudao.youdao.module.member.controller.admin.tag.vo.TagValueSaveReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.tag.MemberTagDO;
import com.htyoudao.youdao.module.member.dal.dataobject.tagvalue.TagValueDO;
import com.htyoudao.youdao.module.member.dal.mysql.tag.MemberTagMapper;
import com.htyoudao.youdao.module.member.dal.mysql.tagvalue.TagValueMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_TAG_VALUE_EXISTS;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_TAG_VALUE_NOT_EXISTS;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.TAG_STORE_VALUE_EXISTS;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.TAG_VALUE_NOT_EXISTS;

/**
 * 标签 Service 实现类
 *
 */
@Service
@Validated
public class TagValueServiceImpl implements TagValueService {

    @Resource
    private TagValueMapper tagValueMapper;

    @Resource
    private MemberTagMapper memberTagMapper;

    @Override
    public Long createTagValue(TagValueSaveReqVO createReqVO) {
        // 插入
        TagValueDO tagValue = BeanUtils.toBean(createReqVO, TagValueDO.class);
        tagValueMapper.insert(tagValue);
        // 返回
        return tagValue.getId();
    }

    @Override
    public void updateTagValue(TagValueSaveReqVO updateReqVO) {
        // 校验存在
        validateTagValueExists(updateReqVO.getId());
        // 更新
        TagValueDO updateObj = BeanUtils.toBean(updateReqVO, TagValueDO.class);
        tagValueMapper.updateById(updateObj);
    }

    @Override
    public void deleteTagValue(Long id) {
        // 校验存在
        validateTagValueExists(id);

        QueryWrapper<MemberTagDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_id", id);
        Long l = memberTagMapper.selectCount(queryWrapper);
        if (l > 0) {
            throw exception(WX_MEMBER_TAG_VALUE_EXISTS);
        }
        // 删除
        tagValueMapper.deleteById(id);
    }

    private void validateTagValueExists(Long id) {
        if (tagValueMapper.selectById(id) == null) {
            throw exception(WX_MEMBER_TAG_VALUE_NOT_EXISTS);
        }
    }

    @Override
    public TagValueDO getTagValue(Long id) {
        return tagValueMapper.selectById(id);
    }

    @Override
    public PageResult<TagValueDO> getTagValuePage(TagValuePageReqVO pageReqVO) {
        return tagValueMapper.selectPage(pageReqVO);
    }

    @Override
    public List<TagValueRespVO> getListByGroupIds(List<Long> groupIds, String name) {
        if (CollectionUtil.isEmpty(groupIds)) {
            return List.of();
        }
        QueryWrapper<TagValueDO> queryWrapper = new QueryWrapper<>();
        if(ObjectUtil.isNotEmpty(name)){
            queryWrapper.in("tag_group_id", groupIds).like("name", name);
        }else {
            queryWrapper.in("tag_group_id", groupIds);
        }
        queryWrapper.orderByDesc("create_time");
        List<TagValueDO> tagValues = tagValueMapper.selectList(queryWrapper);
        return BeanUtils.toBean(tagValues, TagValueRespVO.class);
    }

    @Override
    public Boolean insertBatch(List<TagValueSaveReqVO> tagValueList) {
        // 校验标签值是否存在
        List<String> names = tagValueList.stream().map(TagValueSaveReqVO::getName).toList();
        if (CollectionUtil.isNotEmpty(names)) {
            List<TagValueDO> tagValues = tagValueMapper.selectList(new QueryWrapper<TagValueDO>().in("name", names));
            if (CollectionUtil.isNotEmpty(tagValues)) {
                List<String> tagValueNameList = tagValues.stream().map(TagValueDO::getName).toList();
                String result = String.join(", ", tagValueNameList);
                throw exception(new ErrorCode(1_007_001_030, "标签值已存在"+ result));
            }
        }
        List<TagValueDO> bean = BeanUtils.toBean(tagValueList, TagValueDO.class);
        return tagValueMapper.insertBatch(bean);
    }

    @Override
    public void deleteBatch(List<Long> tagValueIdList) {
        QueryWrapper<TagValueDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", tagValueIdList);
        tagValueMapper.delete(queryWrapper);
    }

    @Override
    public List<TagValueRespVO> getListByGroupId(Long id) {
        LambdaQueryWrapper<TagValueDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TagValueDO::getTagGroupId, id);
        queryWrapper.orderByDesc(TagValueDO::getCreateTime);
        List<TagValueDO> tagValues = tagValueMapper.selectList(queryWrapper);
        return BeanUtils.toBean(tagValues, TagValueRespVO.class);
    }

    @Override
    public void deleteByTagGroupId(Long id) {
        QueryWrapper<TagValueDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_group_id", id);
        tagValueMapper.delete(queryWrapper);
    }

    @Override
    public void updateBatch(List<TagValueSaveReqVO> updateValueList) {
        // 校验标签值是否存在
        List<String> names = updateValueList.stream().map(TagValueSaveReqVO::getName).toList();
        List<Long> ids = updateValueList.stream().map(TagValueSaveReqVO::getId).toList();
        if (CollectionUtil.isNotEmpty(names)) {
            List<TagValueDO> tagValues = tagValueMapper.selectList(new QueryWrapper<TagValueDO>().in("name", names).notIn("id", ids));
            if (CollectionUtil.isNotEmpty(tagValues)) {
                List<String> tagValueNameList = tagValues.stream().map(TagValueDO::getName).toList();
                String result = String.join(", ", tagValueNameList);
                throw exception(new ErrorCode(1_007_001_030, "标签值已存在"+ result));
            }
        }
        List<TagValueDO> entityList = BeanUtils.toBean(updateValueList, TagValueDO.class);
        tagValueMapper.updateBatch(entityList);
    }

    /**
     * 根据名称查询标签
     */
    @Override
    public List<TagValueDO> selectListByTagName(String tagName) {
        List<TagValueDO> tagValues = tagValueMapper.selectList(new LambdaQueryWrapperX<TagValueDO>().likeIfPresent(TagValueDO::getName, tagName));
        return tagValues;
    }

    @Override
    public List<Long> getTagGroupIdByValueName(String name) {
        QueryWrapper<TagValueDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        List<TagValueDO> result = tagValueMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(result)) {
            return result.stream().map(TagValueDO::getTagGroupId).toList();
        }
        return List.of();
    }

    @Override
    public List<MemberTagVO> getMemberTageListByMemberId(List<Long> memberIds) {
        return tagValueMapper.getMemberTageListByMemberId(memberIds);
    }
}