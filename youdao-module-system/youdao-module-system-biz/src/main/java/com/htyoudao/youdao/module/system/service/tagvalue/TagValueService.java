package com.htyoudao.youdao.module.system.service.tagvalue;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagValuePageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagValueRespVO;
import com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagValueSaveReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.tagvalue.TagValueDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 标签 Service 接口
 *
 * @author 零零玖零
 */
public interface TagValueService {

    /**
     * 创建标签
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTagValue(@Valid TagValueSaveReqVO createReqVO);

    /**
     * 更新标签
     *
     * @param updateReqVO 更新信息
     */
    void updateTagValue(@Valid TagValueSaveReqVO updateReqVO);

    /**
     * 删除标签
     *
     * @param id 编号
     */
    void deleteTagValue(Long id);

    /**
     * 获得标签
     *
     * @param id 编号
     * @return 标签
     */
    TagValueDO getTagValue(Long id);

    /**
     * 获得标签分页
     *
     * @param pageReqVO 分页查询
     * @return 标签分页
     */
    PageResult<TagValueDO> getTagValuePage(TagValuePageReqVO pageReqVO);

    /**
     * 获得标签分页（返回 Response VO）
     * @param pageReqVO 分页查询
     * @return 标签分页 Response VO
     */
    PageResult<com.htyoudao.youdao.module.system.controller.admin.tag.vo.TagValueRespVO> getTagValuePageResp(TagValuePageReqVO pageReqVO);

    /**
     * 根据标签组id查询标签集合
     * @param groupIds 标签组id集合
     * @param name 标签名称
     * @return List
     */
    List<TagValueRespVO> getListByGroupIds(List<Long> groupIds,String name);

    /**
     * 批量插入标签集合
     * @param tagValueList 标签集合
     * @return Boolean
     */
    Boolean insertBatch(List<TagValueSaveReqVO> tagValueList);

    /**
     * 批量删除标签集合
     * @param tagValueIdList tagValueIdList
     */
    void deleteBatch(List<Long> tagValueIdList);

    /**
     * 根据标签组id查询标签集合
     * @param id 标签组id
     * @return List
     */
    List<TagValueRespVO> getListByGroupId(Long id);

    /**
     * 根据标签组id删除标签集合
     * @param id 标签组id
     */
    void deleteByTagGroupId(Long id);

    /**
     * 批量修改标签集合
     * @param updateValueList updateValueList
     */
    void updateBatch(List<TagValueSaveReqVO> updateValueList);


    /**
     * 根据名称查询标签
     */
    List<TagValueDO> selectListByTagName(String tagName);


    /**
     * 根据标签值名称查询标签组id
     * @param name name
     * @return Long
     */
    List<Long> getTagGroupIdByValueName(String name);

    /**
     * 根据标签值 ids 批量获取 id -> name 映射
     * @param ids 标签值 id 列表
     * @return id -> name 映射
     */
    Map<Long, String> getNamesByIds(List<Long> ids);
}