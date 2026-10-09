package com.htyoudao.youdao.module.system.service.storebackground;

import com.htyoudao.youdao.module.system.controller.admin.store.vo.background.StoreBackgroundRespVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.background.StoreBackgroundSaveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.background.StoreBackgroundStatusReqVO;

import java.util.List;

/**
 * 门店背景模板服务。
 */
public interface StoreBackgroundService {

    /**
     * 查询全部门店背景模板。
     *
     * @return 门店背景模板列表
     */
    List<StoreBackgroundRespVO> getList();

    /**
     * 查询门店背景模板详情。
     *
     * @param id 模板编号
     * @return 门店背景模板详情
     */
    StoreBackgroundRespVO get(Long id);

    /**
     * 创建关闭状态的自定义门店背景模板。
     *
     * @param reqVO 模板保存参数
     * @return 模板编号
     */
    Long create(StoreBackgroundSaveReqVO reqVO);

    /**
     * 修改门店背景模板。
     *
     * @param reqVO 模板保存参数
     */
    void update(StoreBackgroundSaveReqVO reqVO);

    /**
     * 修改自定义门店背景模板发布状态，并更新发布时间。
     *
     * @param reqVO 模板状态参数
     */
    void updateStatus(StoreBackgroundStatusReqVO reqVO);

    /**
     * 删除关闭状态的自定义门店背景模板。
     *
     * @param id 模板编号
     */
    void delete(Long id);
}
