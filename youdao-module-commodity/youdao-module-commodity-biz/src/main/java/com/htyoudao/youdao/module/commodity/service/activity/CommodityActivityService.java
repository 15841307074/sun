package com.htyoudao.youdao.module.commodity.service.activity;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityActivityDTO;
import com.htyoudao.youdao.module.commodity.api.VO.CommodityActivityVO;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.CommodityActivityPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.CommodityActivityRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.activity.vo.CommodityActivitySaveReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.activity.CommodityActivityDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 商品活动 Service 接口
 *
 * @author hhhh
 */
public interface CommodityActivityService {

    /**
     * 创建商品活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Boolean createActivity(@Valid CommodityActivitySaveReqVO createReqVO);

    /**
     * 删除商品活动
     *
     * @param id 编号
     */
    void deleteActivity(Long id);

    /**
     * 获得商品活动分页
     *
     * @param pageReqVO 分页查询
     * @return 商品活动分页
     */
    PageResult<CommodityActivityDO> getActivityPage(CommodityActivityPageReqVO pageReqVO);

    /**
     * 获得商品活动列表
     *
     * @return List<CommodityActivityRespVO>
     */
    List<CommodityActivityRespVO> selcetSpusForActivity();

    void updateCommodityName(@NotNull(message = "商品ID不能为空") Long commodityId, @NotEmpty(message = "商品名称不能为空") String commodityName);

    void updateCommodityNameAndImage(@NotNull(message = "商品ID不能为空") Long commodityId, @NotEmpty(message = "商品名称不能为空") String commodityName, @NotEmpty(message = "商品图片不能为空") String image);

    /**
     * 获取商品活动列表
     *
     * @param body
     * @return
     */
    List<CommodityActivityDTO> getCommodityActivityList(CommodityActivityVO body);
}