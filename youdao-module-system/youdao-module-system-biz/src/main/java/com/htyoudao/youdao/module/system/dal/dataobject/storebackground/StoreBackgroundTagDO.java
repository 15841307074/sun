package com.htyoudao.youdao.module.system.dal.dataobject.storebackground;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门店背景模板与标签关系 DO。
 *
 * 当模板按标签应用时，用于保存模板选择的标签。
 */
@TableName("system_store_background_tag")
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreBackgroundTagDO extends BusinessBaseDO {

    /** 关系编号。 */
    @TableId
    private Long id;
    /** 门店背景模板编号。 */
    private Long backgroundId;
    /** 门店标签编号。 */
    private Long tagId;
}
