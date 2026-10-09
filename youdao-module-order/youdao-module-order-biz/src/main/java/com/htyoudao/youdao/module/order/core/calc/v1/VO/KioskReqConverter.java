package com.htyoudao.youdao.module.order.core.calc.v1.VO;

import com.htyoudao.youdao.module.order.core.calc.v2.VO.KioskSubmitReqV2VO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

public class KioskReqConverter {

    /**
     * KioskSubmitReqVO -> KioskSubmitReqV2VO
     */
    public static KioskSubmitReqV2VO toV2(KioskSubmitReqVO v1) {
        if (v1 == null) return null;

        KioskSubmitReqV2VO v2 = new KioskSubmitReqV2VO();
        BeanUtils.copyProperties(v1, v2, "settlementInfo");

        if (v1.getSettlementInfo() != null) {
            v2.setSettlementInfo(toV2(v1.getSettlementInfo()));
        }

        return v2;
    }

    /**
     * SettlementReqVO -> SettlementReqV2VO
     */
    public static SettlementReqV2VO toV2(SettlementReqVO v1) {
        if (v1 == null) return null;

        SettlementReqV2VO v2 = new SettlementReqV2VO();

        // 基础字段复制（排除 commodityInfos）
        BeanUtils.copyProperties(v1, v2, "commodityInfos");

        // 复制秒杀信息
        if (v1.getSeckillInfo() != null) {
            SettlementReqV2VO.SeckillInfo s2 = new SettlementReqV2VO.SeckillInfo();
            BeanUtils.copyProperties(v1.getSeckillInfo(), s2);
            v2.setSeckillInfo(s2);
        }

        // 复制商品集合
        if (v1.getCommodityInfos() != null && !v1.getCommodityInfos().isEmpty()) {
            List<SettlementReqV2VO.CommodityInfoVO> commodityList =
                    v1.getCommodityInfos()
                            .stream()
                            .map(KioskReqConverter::convertCommodity)
                            .collect(Collectors.toList());

            v2.setCommodityInfos(commodityList);
        }

        return v2;
    }

    /**
     * 商品转换
     */
    private static SettlementReqV2VO.CommodityInfoVO convertCommodity(
            SettlementReqVO.CommodityInfoVO c1) {

        if (c1 == null) return null;

        SettlementReqV2VO.CommodityInfoVO c2 =
                new SettlementReqV2VO.CommodityInfoVO();

        BeanUtils.copyProperties(c1, c2, "singleList", "singleFlavorList");

        // 转换 singleList -> singleFlavorList
        c2.setSingleFlavorList(convertSingleList(c1.getSingleList()));

        // 复制 flavorList
        c2.setFlavorList(copyFlavors(c1.getFlavorList()));

        // 复制 condimentsList
        c2.setCondimentsList(copyCondiments(c1.getCondimentsList()));

        return c2;
    }

    /**
     * singleList -> singleFlavorList
     */
    private static List<SettlementReqV2VO.SingleFlavorVO> convertSingleList(
            List<Map<Long, Integer>> singleList) {

        if (singleList == null || singleList.isEmpty()) {
            return new ArrayList<>();
        }

        List<SettlementReqV2VO.SingleFlavorVO> result = new ArrayList<>();

        for (Map<Long, Integer> map : singleList) {
            if (map == null || map.isEmpty()) continue;

            for (Map.Entry<Long, Integer> entry : map.entrySet()) {
                SettlementReqV2VO.SingleFlavorVO vo =
                        new SettlementReqV2VO.SingleFlavorVO();

                vo.setSingleId(entry.getKey());
                vo.setNum(entry.getValue());
                vo.setFlavors(new ArrayList<>()); // V1 无子项属性，默认空

                result.add(vo);
            }
        }

        return result;
    }

    private static List<SettlementReqV2VO.FlavorInfoVO> copyFlavors(
            List<SettlementReqVO.FlavorInfoVO> list) {

        if (list == null || list.isEmpty()) return new ArrayList<>();

        return list.stream().map(f1 -> {
            SettlementReqV2VO.FlavorInfoVO f2 =
                    new SettlementReqV2VO.FlavorInfoVO();
            f2.setName(f1.getName());
            f2.setValue(f1.getValue());
            return f2;
        }).collect(Collectors.toList());
    }

    private static List<SettlementReqV2VO.CondimentInfoVO> copyCondiments(
            List<SettlementReqVO.CondimentInfoVO> list) {

        if (list == null || list.isEmpty()) return new ArrayList<>();

        return list.stream().map(c1 -> {
            SettlementReqV2VO.CondimentInfoVO c2 =
                    new SettlementReqV2VO.CondimentInfoVO();
            c2.setId(c1.getId());
            c2.setNumber(c1.getNumber());
            return c2;
        }).collect(Collectors.toList());
    }
}
