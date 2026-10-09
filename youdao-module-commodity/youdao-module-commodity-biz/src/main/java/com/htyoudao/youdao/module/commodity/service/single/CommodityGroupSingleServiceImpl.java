package com.htyoudao.youdao.module.commodity.service.single;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.CommodityUNameReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusUpdateReqVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityGroupSingle;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityGroupSingleMapper;
import com.htyoudao.youdao.module.commodity.enums.SpuEnum;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class CommodityGroupSingleServiceImpl  implements ICommodityGroupSingleService {


    @Resource
    private CommodityGroupSingleMapper commodityGroupSingleMapper;

    @Override
    public void saveBatch(List<CommodityGroupSingle> commodityGroupSingleList) {
        if (ObjectUtil.isNotEmpty(commodityGroupSingleList)) {
            commodityGroupSingleMapper.insertBatch(commodityGroupSingleList);
        }
    }

    @Override
    public void deleteBySpuId(Long commodityId) {
        LambdaQueryWrapper<CommodityGroupSingle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityGroupSingle::getSpuId, commodityId);
        commodityGroupSingleMapper.delete(queryWrapper);
    }

    @Override
    public List<CommodityGroupSingle> selectBySpuId(Long commodityId) {

        LambdaQueryWrapper<CommodityGroupSingle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityGroupSingle::getSpuId, commodityId);

        return commodityGroupSingleMapper.selectList(queryWrapper);
    }

    /**
     * 查找这些商品 id 所指的 single 品（香辣鸡腿堡 id->香辣鸡腿堡 single）
     * @param commodityIds
     * @return
     */
    @Override
    public List<CommodityGroupSingle> selectBySpuIds(List<Long> commodityIds) {

        LambdaQueryWrapper<CommodityGroupSingle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommodityGroupSingle::getCommodityId, commodityIds);
        return commodityGroupSingleMapper.selectList(queryWrapper);
    }

    @Override
    public void updateUpInSpuIds(List<Long> commodityIds) {
        LambdaUpdateWrapper<CommodityGroupSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.in(CommodityGroupSingle::getCommodityId, commodityIds);
        singleLambdaUpdateWrapper.set(CommodityGroupSingle::getStoreStatus, 1);
        singleLambdaUpdateWrapper.set(CommodityGroupSingle::getWxStatus, 1);
        commodityGroupSingleMapper.update(singleLambdaUpdateWrapper);
    }

    @Override
    public List<CommodityGroupSingle> selectByGroupIds(List<Long> setmealIds) {
        LambdaQueryWrapper<CommodityGroupSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.in(CommodityGroupSingle::getGroupId, setmealIds);
        if (CollectionUtils.isEmpty(setmealIds)){
            return new ArrayList<>();
        }
        return  commodityGroupSingleMapper.selectList(singleLambdaQueryWrapper);

    }

    @Override
    public void updateDownBySpuIds(List<Long> commodityIds) {
        LambdaUpdateWrapper<CommodityGroupSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.in(CommodityGroupSingle::getCommodityId, commodityIds);
        singleLambdaUpdateWrapper.set(CommodityGroupSingle::getStoreStatus, 0);
        singleLambdaUpdateWrapper.set(CommodityGroupSingle::getWxStatus, 0);

        commodityGroupSingleMapper.update(singleLambdaUpdateWrapper);
    }


    @Override
    public void updateByIds(List<CommodityGroupSingle> singles) {
        commodityGroupSingleMapper.updateBatch(singles);
    }

    @Override
    public void deleteBySpuIds(List<Long> commodityIds) {
        LambdaQueryWrapper<CommodityGroupSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.in(CommodityGroupSingle::getSpuId, commodityIds);
        commodityGroupSingleMapper.delete(singleLambdaQueryWrapper);
    }


    @Override
    public void updateSpuNameBySpuId(CommodityUNameReqVo uNameReqVo) {
        /*LambdaUpdateWrapper<CommodityGroupSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityGroupSingle::getCommodityId, uNameReqVo.getCommodityId());
        singleLambdaUpdateWrapper.set(CommodityGroupSingle::getCommodityName, uNameReqVo.getCommodityName());
        commodityGroupSingleMapper.update(singleLambdaUpdateWrapper);*/
        String commodityName = uNameReqVo.getCommodityName();
        LambdaQueryWrapper<CommodityGroupSingle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityGroupSingle::getCommodityId, uNameReqVo.getCommodityId());
        List<CommodityGroupSingle> commodityGroupSingles = commodityGroupSingleMapper.selectList(queryWrapper);
        if (ObjectUtil.isNotEmpty(commodityGroupSingles)) {
            for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingles) {
                String oldName = commodityGroupSingle.getCommodityName();
                String newName ;

                if (oldName.contains("（") && oldName.contains("）")) {
                    // 提取括号部分（例如"（中）"）
                    String suffix = oldName.substring(oldName.indexOf("（"));
                    // 组合新名称：新前缀 + 原有括号部分
                    newName = commodityName + suffix;
                } else {
                    // 不包含括号，直接替换为新名称
                    newName = commodityName;
                }
                commodityGroupSingle.setCommodityName(newName);
            }
            commodityGroupSingleMapper.updateBatch(commodityGroupSingles);
        }


    }

    @Override
    public List<CommodityGroupSingle> selectByChooseViewAndSpuId(Long commodityId, Integer chooseView) {
        LambdaQueryWrapper<CommodityGroupSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.eq(CommodityGroupSingle::getCommodityId, commodityId);
        if (chooseView.equals(1)){
            singleLambdaQueryWrapper.eq(CommodityGroupSingle::getWxStatus, 1);
        }else {
            singleLambdaQueryWrapper.eq(CommodityGroupSingle::getStoreStatus, 1);
        }


        return commodityGroupSingleMapper.selectList(singleLambdaQueryWrapper);
    }

    @Override
    public void updateDownBySpuId(Integer chooseView, Long commodityId) {
        LambdaUpdateWrapper<CommodityGroupSingle> singleLambdaQueryWrapper = new LambdaUpdateWrapper<>();
        singleLambdaQueryWrapper.eq(CommodityGroupSingle::getCommodityId, commodityId);
        if (chooseView.equals(1)){
            singleLambdaQueryWrapper.set(CommodityGroupSingle::getWxStatus,0);
        }else {
            singleLambdaQueryWrapper.set(CommodityGroupSingle::getStoreStatus,0);
        }
        commodityGroupSingleMapper.update(singleLambdaQueryWrapper);

    }

    @Override
    public void updateUpBySpuId(Integer chooseView, Long commodityId) {
        LambdaUpdateWrapper<CommodityGroupSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityGroupSingle::getCommodityId, commodityId);
        if (chooseView.equals(1)){
            singleLambdaUpdateWrapper.set(CommodityGroupSingle::getWxStatus,1);
        }else {
            singleLambdaUpdateWrapper.set(CommodityGroupSingle::getStoreStatus,1);
        }

        commodityGroupSingleMapper.update(singleLambdaUpdateWrapper);
    }

    /**
     * 查找套餐下的子品门
     * @param setmealIds
     * @return
     */
    @Override
    public List<CommodityGroupSingle> selectByCommodityIds(List<Long> setmealIds) {
        LambdaQueryWrapper<CommodityGroupSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.in(CommodityGroupSingle::getSpuId, setmealIds);

        return commodityGroupSingleMapper.selectList(singleLambdaQueryWrapper);
    }

    /**
     * 原始id获取子品
     * @param commodityIds
     * @return
     */
    @Override
    public List<CommodityGroupSingle> selectBySpuOriginalIds(List<Long> commodityIds) {
        LambdaQueryWrapper<CommodityGroupSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.in(CommodityGroupSingle::getCommodityId, commodityIds);
        return commodityGroupSingleMapper.selectList(singleLambdaQueryWrapper);
    }

    @Override
    public void changeImageAndName(CommoditySpusUpdateReqVo updateReqVo) {
        LambdaQueryWrapper<CommodityGroupSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.eq(CommodityGroupSingle::getCommodityId, updateReqVo.getCommodityId());
        List<CommodityGroupSingle> commodityGroupSingles = commodityGroupSingleMapper.selectList(singleLambdaQueryWrapper);
        if(ObjectUtil.isNotEmpty(commodityGroupSingles)){
            String image = updateReqVo.getImageUrlVO().get(0);
            String commodityName = updateReqVo.getCommodityName();
            for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingles) {
                commodityGroupSingle.setCommodityUrl(image);
                String oldName = commodityGroupSingle.getCommodityName();
                String newName ;
                // 检查是否包含括号
                if (oldName.contains("(") && oldName.contains(")")) {
                    // 提取括号部分（例如"（中）"）
                    String suffix = oldName.substring(oldName.indexOf("("));
                    // 组合新名称：新前缀 + 原有括号部分
                    newName = commodityName + suffix;
                } else {
                    // 不包含括号，直接替换为新名称
                    newName = commodityName;
                }
                commodityGroupSingle.setCommodityName(newName);
            }

            commodityGroupSingleMapper.updateBatch(commodityGroupSingles);
        }
    }


    @Override
    public void changeName(CommodityUNameReqVo uNameReqVo) {
        LambdaQueryWrapper<CommodityGroupSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.eq(CommodityGroupSingle::getCommodityId, uNameReqVo.getCommodityId());
        List<CommodityGroupSingle> commodityGroupSingles = commodityGroupSingleMapper.selectList(singleLambdaQueryWrapper);
        if(ObjectUtil.isNotEmpty(commodityGroupSingles)){

            String commodityName = uNameReqVo.getCommodityName();
            for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingles) {

                String oldName = commodityGroupSingle.getCommodityName();
                String newName ;
                // 检查是否包含括号
                if (oldName.contains("(") && oldName.contains(")")) {
                    // 提取括号部分（例如"（中）"）
                    String suffix = oldName.substring(oldName.indexOf("("));
                    // 组合新名称：新前缀 + 原有括号部分
                    newName = commodityName + suffix;
                } else {
                    // 不包含括号，直接替换为新名称
                    newName = commodityName;
                }
                commodityGroupSingle.setCommodityName(newName);
            }

            commodityGroupSingleMapper.updateBatch(commodityGroupSingles);
        }
    }

    @Override
    public List<CommodityGroupSingle> selectByChooseViewAndSpuIdForDown(Long commodityId, Integer chooseView) {
        LambdaQueryWrapper<CommodityGroupSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.eq(CommodityGroupSingle::getCommodityId, commodityId);

        return commodityGroupSingleMapper.selectList(singleLambdaQueryWrapper);
    }

    @Override
    public void updateUpOrDownBySpuId(Integer chooseView, List<Long> singleUpId, Integer upOrDown) {
        LambdaUpdateWrapper<CommodityGroupSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.in(CommodityGroupSingle::getCommodityId, singleUpId);
        if (chooseView.equals(SpuEnum.WX.getCode())){
            singleLambdaUpdateWrapper.set(CommodityGroupSingle::getWxStatus,upOrDown);
        }else {
            singleLambdaUpdateWrapper.set(CommodityGroupSingle::getStoreStatus,upOrDown);
        }

        commodityGroupSingleMapper.update(singleLambdaUpdateWrapper);
    }

    @Override
    public void emitSyncToSubProducts(String flavorJson, Long commodityId) {
        LambdaUpdateWrapper<CommodityGroupSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityGroupSingle::getCommodityId, commodityId);
        singleLambdaUpdateWrapper.set(CommodityGroupSingle::getFlavor,flavorJson);
        commodityGroupSingleMapper.update(singleLambdaUpdateWrapper);
    }

    @Override
    public void emitSyncEmptyToSubProducts(Long commodityId) {
        LambdaUpdateWrapper<CommodityGroupSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();

        singleLambdaUpdateWrapper.eq(CommodityGroupSingle::getCommodityId, commodityId);

        singleLambdaUpdateWrapper.setSql("flavor = NULL");

        commodityGroupSingleMapper.update(null, singleLambdaUpdateWrapper);
    }
}
