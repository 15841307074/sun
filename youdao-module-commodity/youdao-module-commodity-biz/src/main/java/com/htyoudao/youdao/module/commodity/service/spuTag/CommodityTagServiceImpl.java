package com.htyoudao.youdao.module.commodity.service.spuTag;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import com.htyoudao.youdao.module.commodity.dal.mysql.spuTag.CommodityTagMapper;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.htyoudao.youdao.module.commodity.service.storeSpu.ICommodityStoreSpuService;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.htyoudao.youdao.module.commodity.controller.admin.spuTag.VO.CommodityTagReqVO;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.TAG_DUPLICATED_TAG_NAMES;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.TAG_HAS_COMMODITY;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;


@Service
public class CommodityTagServiceImpl implements ICommodityTageService{

    @Resource
    private CommodityTagMapper commodityTagMapper;

    @Resource
    private ICommoditySpusService commoditySpusService;

    @Resource
    private ICommodityStoreSpuService commodityStoreSpuService;

    @Override
    @LogRecord(type = "Commodity 商品标签", subType = "创建商品标签", bizNo = "{{#id}}",
            success = "创建了门店【{{#commodityTagReqVO.name}}】")
    public void create(CommodityTagReqVO commodityTagReqVO) {
        //查看是否有重复标签名
        LambdaQueryWrapper<CommodityTag>   commodityTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityTagLambdaQueryWrapper.eq(CommodityTag::getName, commodityTagReqVO.getName());
        List<CommodityTag> commodityTags = commodityTagMapper.selectList(commodityTagLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(commodityTags)) {
            throw exception(TAG_DUPLICATED_TAG_NAMES);
        }
        //没有重复的 存入
        CommodityTag commodityTag = new CommodityTag();
        BeanUtils.copyProperties(commodityTagReqVO, commodityTag);
        commodityTagMapper.insert(commodityTag);

    }


    @Override
    @LogRecord(type = "Commodity 商品标签", subType = "修改商品标签", bizNo = "{{#id}}",
            success = "更新了商品标签【{{#commodityTagReqVO.name}}】: {_DIFF{#commodityTagReqVO}}")
    public void update(CommodityTagReqVO commodityTagReqVO) {
        //查看是否有重复标签名
        LambdaQueryWrapper<CommodityTag>   commodityTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityTagLambdaQueryWrapper.eq(CommodityTag::getName, commodityTagReqVO.getName());
        List<CommodityTag> commodityTags = commodityTagMapper.selectList(commodityTagLambdaQueryWrapper);
        commodityTags.removeIf(commodityTag -> commodityTag.getId().equals(commodityTagReqVO.getId()));
        if (ObjectUtil.isNotEmpty(commodityTags)) {
            throw exception(TAG_DUPLICATED_TAG_NAMES);
        }
        //没有重复的 存入
        CommodityTag commodityTag = new CommodityTag();
        BeanUtils.copyProperties(commodityTagReqVO, commodityTag);
        commodityTagMapper.updateById(commodityTag);
    }

    @Override
    public CommodityTagReqVO getById(Long id) {

        CommodityTag commodityTag = commodityTagMapper.selectById(id);
        CommodityTagReqVO commodityTagReqVO = new CommodityTagReqVO();
        BeanUtils.copyProperties(commodityTag, commodityTagReqVO);

        return commodityTagReqVO;
    }

    @Override
    public List<CommodityTagReqVO> selectList(CommodityTagReqVO commodityTagReqVO) {

        LambdaQueryWrapper<CommodityTag> commodityTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotEmpty(commodityTagReqVO.getName())) {
            commodityTagLambdaQueryWrapper.like(CommodityTag::getName, commodityTagReqVO.getName());
        }
        commodityTagLambdaQueryWrapper.orderByDesc(CommodityTag::getUpdateTime);
        List<CommodityTag> commodityTags = commodityTagMapper.selectList(commodityTagLambdaQueryWrapper);
        List<CommodityTagReqVO> commodityTagReqVOS = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(commodityTags)) {

            for (CommodityTag commodityTag : commodityTags) {
                CommodityTagReqVO commodityTagReqVO1 = new CommodityTagReqVO();
                BeanUtils.copyProperties(commodityTag, commodityTagReqVO1);
                commodityTagReqVOS.add(commodityTagReqVO1);
            }
        }
        return commodityTagReqVOS;
    }

    @Override
    @LogRecord(type = "Commodity 商品标签", subType = "删除商品标签", bizNo = "{{#id}}",
            success = "删除了商品标签{{#id}}")
    public void delById(Long id) {

      Boolean isExist=  commoditySpusService.selectIsExistTag(id);
      if (isExist) {
          throw exception(TAG_HAS_COMMODITY);
      }
        isExist =   commodityStoreSpuService.selectIsExistTag(id);

      if (isExist) {
          throw exception(TAG_HAS_COMMODITY);
      }

        commodityTagMapper.deleteById(id);
    }

    @Override
    public List<CommodityTag> selectListByIds(List<Long> tagIds) {

        LambdaQueryWrapper<CommodityTag> commodityTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityTagLambdaQueryWrapper.in(CommodityTag::getId, tagIds);
        List<CommodityTag> commodityTags = commodityTagMapper.selectList(commodityTagLambdaQueryWrapper);
        List<CommodityTag> commodityTagsNew = new ArrayList<>();
        Map<Long, CommodityTag> collect = commodityTags.stream().collect(Collectors.toMap(CommodityTag::getId, c -> c));
        for (Long tagId : tagIds) {
            commodityTagsNew.add(collect.get(tagId));
        }


        return commodityTagsNew;
    }
}
