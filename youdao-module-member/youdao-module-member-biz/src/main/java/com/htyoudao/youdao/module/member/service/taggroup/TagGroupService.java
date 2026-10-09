package com.htyoudao.youdao.module.member.service.taggroup;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.controller.admin.tag.vo.*;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 标签组 Service 接口
 *
 * @author 零零玖零
 */
public interface TagGroupService {

    /**
     * 创建标签组
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTagGroup(@Valid TagGroupSaveReqVO createReqVO);

    /**
     * 更新标签组
     *
     * @param updateReqVO 更新信息
     */
    void updateTagGroup(@Valid TagGroupUpdateReqVO updateReqVO);

    /**
     * 删除标签组
     *
     * @param id 编号
     */
    void deleteTagGroup(Long id);

    /**
     * 获得标签组
     *
     * @param id 编号
     * @return 标签组
     */
    TagGroupRespVO getTagGroup(Long id);

    /**
     * 获得标签组分页
     *
     * @param pageReqVO 分页查询
     * @return 标签组分页
     */
    PageResult<TagGroupRespVO> getTagGroupPage(TagGroupPageReqVO pageReqVO);
    /**
     * 获得标签组
     */
    List<TagGroupRespVO> getTagGroupByName(String tagName);
    /**
     * 获取标签
     */
    List<TagValueRespVO> getTagList();

    List<TagGroupRespVO> tagList();

    void updateBatchTag(MembersUpdateVO membersUpdateVO);

    void updateDelTag(MembersUpdateVO membersUpdateVO);
}