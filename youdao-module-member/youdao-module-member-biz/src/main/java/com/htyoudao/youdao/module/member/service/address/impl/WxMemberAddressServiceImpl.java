package com.htyoudao.youdao.module.member.service.address.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.enums.WxAddressTypeEnum;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.module.member.controller.app.address.vo.CheckWithinDeliveryRangeReqVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressReqMemberVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressReqVO;
import com.htyoudao.youdao.module.member.controller.app.address.vo.WxMemberAddressRespVO;
import com.htyoudao.youdao.module.member.dal.dataobject.address.WxMemberAddressDO;
import com.htyoudao.youdao.module.member.dal.mysql.address.WxMemberAddressMapper;
import com.htyoudao.youdao.module.member.service.address.WxMemberAddressService;
import com.htyoudao.youdao.module.system.api.dept.DeptDeliveryScopeApi;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptDeliveryScopeDTO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDeliveryDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;

@Slf4j
@Service
public class WxMemberAddressServiceImpl implements WxMemberAddressService {

    @Resource
    private WxMemberAddressMapper wxMemberAddressMapper;

    @DubboReference
    private StoreApi storeApi;

    @DubboReference
    private DeptDeliveryScopeApi deptDeliveryScopeApi;

    @Override
    public List<WxMemberAddressRespVO> getAddressListForMemberId(WxMemberAddressReqMemberVO wxMemberAddressVO) {
        LambdaQueryWrapper<WxMemberAddressDO> addressLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressLambdaQueryWrapper.eq(WxMemberAddressDO::getMemberId, wxMemberAddressVO.getMemberId());
        List<WxMemberAddressDO> wxMemberAddresslist = wxMemberAddressMapper.selectList(addressLambdaQueryWrapper);
        return wxMemberAddresslist
                .stream()
                .map(address ->{
                    WxMemberAddressRespVO wxMemberAddressRespVO = new WxMemberAddressRespVO();
                    BeanUtils.copyProperties(address, wxMemberAddressRespVO);
                    return wxMemberAddressRespVO;
                })
                .toList();
    }

    public static boolean containsEmoji(String input) {
        // 匹配更广泛的 Emoji 范围
        String emojiRegex =
                "[\\p{So}\\p{Cn}\\p{InSupplementaryPrivateUseArea-A}\\p{InSupplementaryPrivateUseArea-B}]";
        return Pattern.compile(emojiRegex).matcher(input).find();
    }

    @Override
    public CommonResult<Integer> insertbyChat(WxMemberAddressReqVO wxMemberAddressVO) {
        if (containsEmoji(wxMemberAddressVO.getAddressDetail())){
            return error(WX_MEMBER_ADDRESS_DETAIL_CONTAINS_EMOJI_ERROR);
        }
        LambdaQueryWrapper<WxMemberAddressDO> allAddressLambdaQueryWrapper = new LambdaQueryWrapper<>();
        allAddressLambdaQueryWrapper.eq(WxMemberAddressDO::getMemberId, wxMemberAddressVO.getMemberId());
        List<WxMemberAddressDO> allList = wxMemberAddressMapper.selectList(allAddressLambdaQueryWrapper);

        extracted(wxMemberAddressVO, allList);

        return success(wxMemberAddressMapper.insert(BeanUtils.toBean(wxMemberAddressVO, WxMemberAddressDO.class)));
    }

    @Override
    public CommonResult<Integer> updateAddressByChat(WxMemberAddressReqVO wxMemberAddressVO) {
        if (containsEmoji(wxMemberAddressVO.getAddressDetail())){
            return error(WX_MEMBER_ADDRESS_DETAIL_CONTAINS_EMOJI_ERROR);
        }
        LambdaQueryWrapper<WxMemberAddressDO> allAddressLambdaQueryWrapper = new LambdaQueryWrapper<>();
        allAddressLambdaQueryWrapper.eq(WxMemberAddressDO::getMemberId, wxMemberAddressVO.getMemberId());
        List<WxMemberAddressDO> allList = wxMemberAddressMapper.selectList(allAddressLambdaQueryWrapper);

        extracted(wxMemberAddressVO, allList);
        LambdaQueryWrapper<WxMemberAddressDO> addressLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressLambdaQueryWrapper.eq(WxMemberAddressDO::getAddressId, wxMemberAddressVO.getAddressId());
        return success(wxMemberAddressMapper.update(BeanUtils.toBean(wxMemberAddressVO, WxMemberAddressDO.class),addressLambdaQueryWrapper));
    }

    @Override
    public Integer deleteAddressByAddressId(WxMemberAddressReqVO wxMemberAddressReqVO) {

        return wxMemberAddressMapper.deleteById(wxMemberAddressReqVO.getAddressId());
    }

    @Override
    public Map<String, Object> getAddressMapForMemberId(CheckWithinDeliveryRangeReqVO checkWithinDeliveryRangeReqVO) {
        // 判断门店是否存在 远程调用
        CommonResult<StoreDeliveryDTO> storeDelivery = storeApi.getStoreDelivery(checkWithinDeliveryRangeReqVO.getStoreId());

        StoreDeliveryDTO storeInfoDTO = storeDelivery.getData();
        if (ObjectUtils.isEmpty(storeInfoDTO)) {
            throw new ServiceException(WX_MEMBER_STOREID_ERROR);
        }

        List<WxMemberAddressRespVO> inList = new ArrayList<>();
        List<WxMemberAddressRespVO> outList = new ArrayList<>();
        WxMemberAddressReqMemberVO wxMemberAddressReqMemberVO = new WxMemberAddressReqMemberVO();
        wxMemberAddressReqMemberVO.setMemberId(checkWithinDeliveryRangeReqVO.getMemberId());
        List<WxMemberAddressRespVO> allList = getAddressListForMemberId(wxMemberAddressReqMemberVO);
        if(CollectionUtils.isEmpty(allList)){
            Map<String, Object> map = new HashMap<>();
            map.put("inAddress", inList);
            map.put("outAddress", outList);
            return map;
        }

        // 跑腿业务 返回全部配送地址
        Integer rangFlag = checkWithinDeliveryRangeReqVO.getRangFlag();
        if (rangFlag != null && rangFlag == 0){
            Map<String, Object> map = new HashMap<>();
            map.put("inAddress", allList);
            map.put("outAddress", outList);
            return map;
        }

        for (WxMemberAddressRespVO wxMemberAddressRespVO: allList){


            DeptDeliveryScopeDTO sysDeptDeliveryScopeParam = new DeptDeliveryScopeDTO();
            sysDeptDeliveryScopeParam.setStoreId(storeInfoDTO.getStoreId());

            CommonResult<List<DeptDeliveryScopeDTO>> deptDeliveryScope = deptDeliveryScopeApi.getDeptDeliveryScope(sysDeptDeliveryScopeParam);
            List<DeptDeliveryScopeDTO> sysDeptDeliveryScopes = deptDeliveryScope.getData();
            Point2D.Double point = new Point2D.Double(wxMemberAddressRespVO.getLongitude().doubleValue(), wxMemberAddressRespVO.getLatitude().doubleValue());
            List<StoreDeliveryDTO> storeInfos = Collections.singletonList(storeInfoDTO);

            //计算是否在可送范围
            this.deliveryStoresBySelectedAddress(sysDeptDeliveryScopes, point, storeInfos);

            if(1 == storeInfos.get(0).getIsDelivery()){
                inList.add(wxMemberAddressRespVO);
            }else {
                outList.add(wxMemberAddressRespVO);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("inAddress", inList);
        map.put("outAddress", outList);

        return map;
    }

    private void deliveryStoresBySelectedAddress(List<DeptDeliveryScopeDTO> sysDeptDeliveryScopes, Point2D.Double point, List<StoreDeliveryDTO> storeInfoList) {
        Map<Long, List<DeptDeliveryScopeDTO>> storeIdToPoint = sysDeptDeliveryScopes.stream().collect(Collectors.groupingBy(DeptDeliveryScopeDTO::getStoreId));
        for (StoreDeliveryDTO sysStoreInfo : storeInfoList) {

            if (ObjectUtils.isEmpty(sysStoreInfo.getStoreLongitude()) || ObjectUtils.isEmpty(sysStoreInfo.getStoreLatitude())) {
                sysStoreInfo.setIsDelivery(0);
            }

            double distance = calculateDistance( point.getX(), point.getY(), sysStoreInfo.getStoreLongitude(), sysStoreInfo.getStoreLatitude());
            sysStoreInfo.setDistance(distance);
            List<DeptDeliveryScopeDTO> scopes = storeIdToPoint.get(sysStoreInfo.getStoreId());
            if (Objects.nonNull(scopes)) {
                List<Point2D.Double> storePointList = scopes.stream().map(scope -> new Point2D.Double(scope.getLongitude(), scope.getLatitude())).toList();
                GeneralPath generalPath = new GeneralPath();
                Point2D.Double aDouble = storePointList.get(0);
                generalPath.moveTo(aDouble.x, aDouble.y);
                storePointList.forEach(deptPoint -> generalPath.lineTo(deptPoint.x, deptPoint.y));
                generalPath.lineTo(aDouble.x, aDouble.y);
                generalPath.closePath();
                if (generalPath.contains(point)) {
                    sysStoreInfo.setIsDelivery(1);
                } else {
                    sysStoreInfo.setIsDelivery(0);
                }
            } else {
                sysStoreInfo.setIsDelivery(0);
            }
        }
    }

    private double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        // 实现计算两个经纬度之间距离的逻辑

        // 返回距离，可以使用 Haversine 公式等方式
        //return 0.0; // 这里返回的是示例值，需要实际根据公式计算
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378138.0;
        s = Math.round(s * 10000d) / 10000d;
        return s;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
    /**
     * 当用户设计默认收货地址时，调整用户其他收货地址状态为非默认
     *
     * @param wxMemberAddressVO 当前操作的地址对象
     * @param allList 用户全部地址集合
     */
    private void extracted(WxMemberAddressReqVO wxMemberAddressVO, List<WxMemberAddressDO> allList) {

        if (wxMemberAddressVO.getDefaultAddress() == WxAddressTypeEnum.DEFAULT.getValue()){
            if (CollectionUtil.isNotEmpty(allList)){
                List<WxMemberAddressDO> list = allList.stream().peek(address -> address.setDefaultAddress(0)).toList();
                wxMemberAddressMapper.updateBatch(list);
            }
        }
    }

}
