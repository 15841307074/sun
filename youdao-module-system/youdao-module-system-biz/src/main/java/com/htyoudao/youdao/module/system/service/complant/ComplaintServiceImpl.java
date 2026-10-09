package com.htyoudao.youdao.module.system.service.complant;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.ip.core.utils.SensitiveUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.order.api.order.BzOrderApi;
import com.htyoudao.youdao.module.order.api.order.dto.OrderDetailRspDTO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintDetailRespVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintRespVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintUpdateReqVO;
import com.htyoudao.youdao.module.system.controller.app.complaint.vo.SysComplaintSaveVO;
import com.htyoudao.youdao.module.system.dal.dataobject.complaint.ComplaintDO;
import com.htyoudao.youdao.module.system.dal.dataobject.complaint.ComplaintImgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storeuser.StoreUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.complaint.ComplaintImgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.complaint.ComplaintMapper;
import com.htyoudao.youdao.module.system.dal.mysql.storeuser.StoreUserMapper;
import com.htyoudao.youdao.module.system.enums.ComplaintStatusEnum;
import com.htyoudao.youdao.module.system.service.org.OrgService;
import com.htyoudao.youdao.module.system.util.complaint.SensitiveWordFilter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;

@Service
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {
    @Resource
    private ComplaintMapper complaintMapper;
    @Resource
    private OrgService orgService;
    @Resource
    private ComplaintImgMapper complaintImgMapper;
    @Resource
    private StoreUserMapper storeUserMapper;
    @DubboReference
    private BzOrderApi orderApi;

    @Override
    public PageResult<ComplaintRespVO> getComplaintListPage(ComplaintPageReqVO pageReqVO) {
        // 查询当前登入人所在组织以及下级
        Set<Long> storeId = new HashSet<>();
        storeId = storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>().eq(StoreUserDO::getUserId, WebFrameworkUtils.getLoginUserId())
                .eq(StoreUserDO::getDeleted, 0)).stream().map(StoreUserDO::getStoreId).collect(Collectors.toSet());
        Set<Long> storeList = new HashSet<>();
        if (pageReqVO.getOrgId() != null) {
            if (pageReqVO.getIsStore() == 0) {
                storeList = orgService.getStoreIdListByOrgID(pageReqVO.getOrgId());
            }
            storeList.add(pageReqVO.getOrgId());
        } else {
            storeList = orgService.getStoreIdsByUser();
            if (CollectionUtil.isNotEmpty(storeId)) {
                if (CollectionUtil.isEmpty(storeList)) {
                    storeList = new HashSet<>();
                }
                storeList.addAll(storeId);
                pageReqVO.setOrgIds(storeId);
            }
        }
        if (CollectionUtil.isNotEmpty(storeList)) {
            pageReqVO.setOrgIds(storeList);
        }
        PageResult<ComplaintDO> pageResult = complaintMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, ComplaintRespVO.class);
    }

    @Override
    public ComplaintDetailRespVO getComplaint(Long id) {
        ComplaintDO complaintDO = complaintMapper.selectById(id);
        ComplaintDetailRespVO complaintDetailRespVO = new ComplaintDetailRespVO();
        if (complaintDO != null) {
            complaintDetailRespVO = BeanUtil.toBean(complaintDO, ComplaintDetailRespVO.class);
            List<String> imgUrl = complaintImgMapper.selectList(new LambdaQueryWrapperX<ComplaintImgDO>().eq(ComplaintImgDO::getComplaintId, id)).stream().map(ComplaintImgDO::getImgUrl).toList();
            complaintDetailRespVO.setImgUrl(imgUrl);
        }
        return complaintDetailRespVO;
    }

    @Override
    public void updateComplaint(ComplaintUpdateReqVO reqVO) {
        if (containsEmoji(reqVO.getDealNote())) {
            throw exception(COMPLAINT_CONTAINS_EMOJI_ERROR);
        }
        ComplaintDO complaintDO = BeanUtil.toBean(reqVO, ComplaintDO.class);
        complaintDO.setDealTime(new Date());
        complaintDO.setComplaintType(ComplaintStatusEnum.PROCESSED.getStatus());
        complaintMapper.updateById(complaintDO);
    }

    @Override
    public Long getComplaintCount() {
        // 查询当前登入人所在组织以及下级
        Set<Long> storeList = orgService.getStoreIdsByUser();
        LambdaQueryWrapperX<ComplaintDO> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eq(ComplaintDO::getComplaintType, ComplaintStatusEnum.NOTPROCESSED.getStatus());
        queryWrapperX.inIfPresent(ComplaintDO::getStoreId, storeList);
        return complaintMapper.selectCount(queryWrapperX);
    }

    /**
     * 新增投诉
     */
    @Override
    public void addComplaint(SysComplaintSaveVO sysComplaintVo) {
        if (StringUtils.isEmpty(sysComplaintVo.getOrderSn())) {
            throw exception(ORDER_NOT_EXISTS);
        }
        OrderDetailRspDTO orderDetailRspDTO = orderApi.getOrderDetail(sysComplaintVo.getOrderSn());
        if(orderDetailRspDTO!=null){
            int orderState = orderDetailRspDTO.getOrderState();
            if (orderState==0||orderState==10||orderState==70) {
                throw exception(ORDER_ERR_EXISTS);
            }
        }

        if (containsEmoji(sysComplaintVo.getComplaintNote())) {
            throw exception(COMPLAINT_CONTAINS_EMOJI_ERROR);
        }
        LocalDate today = LocalDate.now();
        Date todayStart = Date.from(today.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayEnd = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        ComplaintDO isSysComplaint = complaintMapper.selectOne(new LambdaQueryWrapper<ComplaintDO>()
                .eq(ComplaintDO::getMemberId, sysComplaintVo.getMemberId())
                .eq(ComplaintDO::getOrderSn, sysComplaintVo.getOrderSn())
                .between(ComplaintDO::getCreateTime, todayStart, todayEnd));

        if (!Objects.isNull(isSysComplaint)) {
            throw exception(COMPLAINT_EXISTS);
        }
        if (StringUtils.isEmpty(sysComplaintVo.getComplaintNote())) {
            throw exception(COMPLAINT_NOT_EXISTS);
        }
        ComplaintDO sysComplaint = new ComplaintDO();
        BeanUtils.copyProperties(sysComplaintVo, sysComplaint);
        sysComplaint.setComplaintType(0);
        sysComplaint.setBusinessType(sysComplaintVo.getBusinessType());
        sysComplaint.setBusinessId(BusinessContextHolder.getBusinessId());
        //屏蔽关键字
        sysComplaint.setComplaintNote(SensitiveUtils.replaceSensitiveWords(sysComplaint.getComplaintNote()));
        complaintMapper.insert(sysComplaint);
        if (sysComplaintVo.getImgUrl() != null && sysComplaintVo.getImgUrl().size() > 0) {
            List<ComplaintImgDO> sysComplaintImgs = sysComplaintVo.getImgUrl().stream().map(imgUrl -> {
                ComplaintImgDO sysComplaintImg = new ComplaintImgDO();
                sysComplaintImg.setComplaintId(sysComplaint.getId());
                sysComplaintImg.setImgUrl(imgUrl);
                return sysComplaintImg;
            }).collect(Collectors.toList());
            complaintImgMapper.insertBatch(sysComplaintImgs);
        }
    }

    @Override
    public PageResult<ComplaintRespVO> getComplaintListPageApp(ComplaintPageReqVO pageReqVO) {
        // 查询当前登入人所在组织以及下级
        Set<Long> storeList = new HashSet<>();
        if (pageReqVO.getOrgId() != null) {
            if (pageReqVO.getIsStore() == 0) {
                storeList = orgService.getStoreIdListByOrgID(pageReqVO.getOrgId());
            }
            storeList.add(pageReqVO.getOrgId());
        } else {
            storeList = orgService.getStoreIdsByUser();
        }
        if (CollectionUtil.isNotEmpty(storeList)) {
            pageReqVO.setOrgIds(storeList);
        }

        PageResult<ComplaintDO> pageResult = complaintMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, ComplaintRespVO.class);
    }

    public static boolean containsEmoji(String input) {
        // 匹配更广泛的 Emoji 范围
        String emojiRegex =
                "[\\p{So}\\p{Cn}\\p{InSupplementaryPrivateUseArea-A}\\p{InSupplementaryPrivateUseArea-B}]";
        return Pattern.compile(emojiRegex).matcher(input).find();
    }
    @Override
    public  Map<Integer, Long> pageByStoreSum(ComplaintPageReqVO pageReqVO){
        Set<Long> storeList = new HashSet<>();
        if (pageReqVO.getOrgId() != null) {
            if (pageReqVO.getIsStore() == 0) {
                storeList = orgService.getStoreIdListByOrgID(pageReqVO.getOrgId());
            }
            storeList.add(pageReqVO.getOrgId());
        } else {
            storeList = orgService.getStoreIdsByUser();
        }
        if (CollectionUtil.isNotEmpty(storeList)) {
            pageReqVO.setOrgIds(storeList);
        }
        // 提前计算半年前的时间（若需要则使用，避免重复计算）
        LocalDateTime halfYearAgo = Boolean.TRUE.equals(pageReqVO.getIsBoss())
                ? LocalDateTime.now().minusMonths(6)
                : null;

        // 提取公共方法，根据complaintType查询数量
        Long count = getCountByType(pageReqVO, 0, halfYearAgo);
        Long dealCount = getCountByType(pageReqVO, 1, halfYearAgo);

        Map<Integer, Long> map = new HashMap<>(2); // 初始化容量为2，更高效
        map.put(0, count);
        map.put(1, dealCount);
        return map;
    }

    /**
     * 公共查询方法：根据complaintType和条件查询数量
     * @param pageReqVO 请求参数
     * @param complaintType 投诉类型（0或1）
     * @param halfYearAgo 半年前的时间（null则不添加该条件）
     * @return 符合条件的数量
     */
    private Long getCountByType(ComplaintPageReqVO pageReqVO, int complaintType, LocalDateTime halfYearAgo) {
        LambdaQueryWrapperX<ComplaintDO> queryWrapper = new LambdaQueryWrapperX<ComplaintDO>()
                .eq(ComplaintDO::getComplaintType, complaintType) // 固定条件，无需ifPresent
                .eqIfPresent(ComplaintDO::getBusinessType, pageReqVO.getType())
                .eqIfPresent(ComplaintDO::getMemberId, pageReqVO.getMemberId())
                .inIfPresent(ComplaintDO::getStoreId, pageReqVO.getOrgIds());
        // 排序对count查询无意义，可移除（提高效率）

        // 若需要添加半年内条件，则补充（halfYearAgo不为null时）
        if (halfYearAgo != null&&complaintType == 1) {
            queryWrapper.ge(ComplaintDO::getCreateTime, halfYearAgo);
        }

        return complaintMapper.selectCount(queryWrapper);
    }
}