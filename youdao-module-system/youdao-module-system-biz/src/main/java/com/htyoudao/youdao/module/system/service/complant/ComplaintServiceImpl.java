package com.htyoudao.youdao.module.system.service.complant;

import com.htyoudao.youdao.module.system.enums.ComplaintTypeEnum;
import com.htyoudao.youdao.module.system.dal.mysql.user.AdminUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.tagvalue.TagValueMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreTagMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.orguser.OrgUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.tagvalue.TagValueDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.orguser.OrgUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintExportVO;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;

@Service
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {
    @Resource private com.htyoudao.youdao.module.system.service.store.StoreDataScopeService storeDataScopeService;
    @Resource private SystemStoreInfoMapper systemStoreInfoMapper;
    @Resource private OrgMapper orgMapper;
    @Resource private OrgUserMapper orgUserMapper;
    @Resource private AdminUserMapper adminUserMapper;
    @Resource private SystemStoreTagMapper systemStoreTagMapper;
    @Resource private TagValueMapper tagValueMapper;
    @Resource private ExcelActionService<ComplaintExportVO> complaintExportExcelActionService;
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

    @Override
    public Long getRegionPendingCount() {
        Long userId = WebFrameworkUtils.getLoginUserId();
        if (userId == null) throw exception(UNAUTHORIZED);
        long businessId = BusinessContextHolder.getRequiredBusinessId();
        Set<Long> scope = orgService.getStoreIdsByUser();
        Set<Long> storeIds = new HashSet<>();
        if (CollectionUtil.isNotEmpty(scope)) storeIds.addAll(scope);
        // 与 PC 列表范围一致：组织门店与本人直接关联门店取并集；type=1 为店长。
        storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>()
                        .eq(StoreUserDO::getUserId, userId)
                        .eq(StoreUserDO::getBusinessId, businessId)
                        .eq(StoreUserDO::getType, 1)
                        .eq(StoreUserDO::getDeleted, false))
                .stream().map(StoreUserDO::getStoreId).forEach(storeIds::add);
        storeIds.removeIf(id -> id == null || id <= 0);
        if (storeIds.isEmpty()) return 0L;
        // 必须使用非空门店范围；不能用 inIfPresent，否则空权限会退化成统计全项目。
        return complaintMapper.selectCount(new LambdaQueryWrapperX<ComplaintDO>()
                .eq(ComplaintDO::getBusinessId, businessId)
                .eq(ComplaintDO::getComplaintType, ComplaintStatusEnum.NOTPROCESSED.getStatus())
                .eq(ComplaintDO::getDeleted, false)
                .in(ComplaintDO::getStoreId, storeIds));
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
    private void applyAdminComplaintScope(ComplaintPageReqVO pageReqVO) {
        // 查询当前登入人所在组织以及下级
        Set<Long> storeId = storeUserMapper.selectList(new LambdaQueryWrapperX<StoreUserDO>()
                .eq(StoreUserDO::getUserId, WebFrameworkUtils.getLoginUserId())
                .eq(StoreUserDO::getDeleted, 0)).stream().map(StoreUserDO::getStoreId).collect(Collectors.toSet());
        Set<Long> storeList = new HashSet<>();
        if (pageReqVO.getOrgId() != null) {
            if (Objects.equals(pageReqVO.getIsStore(), 0)) {
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
    }

    @Override
    public void exportComplaintList(ComplaintPageReqVO pageReqVO) {
        applyComplaintExportScope(pageReqVO);
        Page<ComplaintExportVO> page = new Page<>(pageReqVO.getPageNo(), 100);
        complaintExportExcelActionService.exportAsyncExcel(ComplaintExportVO.class, page,
                currentPage -> selectComplaintExportList(currentPage, pageReqVO), "投诉列表");
    }

    private void applyComplaintExportScope(ComplaintPageReqVO pageReqVO) {
        Set<Long> allowed = storeDataScopeService.getAccessibleStoreIds(
                BusinessContextHolder.getBusinessId(), WebFrameworkUtils.getLoginUserId());
        if (allowed == null) {
            applyAdminComplaintScope(pageReqVO);
            return;
        }
        Set<Long> scope = new HashSet<>(allowed);
        if (pageReqVO.getOrgId() != null) {
            Set<Long> selected = new HashSet<>();
            if (Objects.equals(pageReqVO.getIsStore(), 0)) {
                Set<Long> organizationStores = orgService.getStoreIdListByOrgID(pageReqVO.getOrgId());
                if (organizationStores != null) selected.addAll(organizationStores);
            } else {
                selected.add(pageReqVO.getOrgId());
            }
            scope.retainAll(selected);
        }
        if (pageReqVO.getOrgIds() != null && !pageReqVO.getOrgIds().isEmpty()) {
            scope.retainAll(pageReqVO.getOrgIds());
        }
        pageReqVO.setOrgIds(scope.isEmpty() ? Set.of(-1L) : Set.copyOf(scope));
    }

    private List<ComplaintExportVO> selectComplaintExportList(Page<ComplaintExportVO> page,
                                                               ComplaintPageReqVO pageReqVO) {
        pageReqVO.setPageNo(Math.toIntExact(page.getCurrent()));
        pageReqVO.setPageSize(Math.toIntExact(page.getSize()));
        List<ComplaintDO> complaints = complaintMapper.selectPage(pageReqVO).getList();
        if (CollectionUtil.isEmpty(complaints)) {
            return Collections.emptyList();
        }

        Set<Long> storeIds = complaints.stream().map(ComplaintDO::getStoreId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SystemStoreInfoDO> storeMap = CollectionUtil.isEmpty(storeIds)
                ? Collections.emptyMap()
                : systemStoreInfoMapper.selectBatchIds(storeIds).stream()
                .collect(Collectors.toMap(SystemStoreInfoDO::getStoreId, store -> store,
                        (first, ignored) -> first));

        Set<Long> orgIds = storeMap.values().stream().map(SystemStoreInfoDO::getOrgId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, OrgDO> orgMap = loadOrganizationMap(orgIds);
        Map<Long, AdminUserDO> orgLeaderMap = loadOrganizationLeaderMap(orgMap.keySet());
        Map<Long, String> storeTagMap = loadStoreTagMap(storeIds);
        Map<Long, List<String>> complaintImageMap = loadComplaintImageMap(complaints);

        Map<Integer, String> complaintTypeNames = Arrays.stream(ComplaintTypeEnum.values())
                .collect(Collectors.toMap(ComplaintTypeEnum::getStatus, ComplaintTypeEnum::getName));
        Map<Integer, String> complaintStatusNames = Arrays.stream(ComplaintStatusEnum.values())
                .collect(Collectors.toMap(ComplaintStatusEnum::getStatus, ComplaintStatusEnum::getName));

        return complaints.stream().map(complaint -> {
            ComplaintExportVO export = BeanUtils.toBean(complaint, ComplaintExportVO.class);
            SystemStoreInfoDO store = storeMap.get(complaint.getStoreId());
            if (store != null) {
                OrgDO org = orgMap.get(store.getOrgId());
                export.setOrgNameAndLeader(formatOrganizationAndLeader(org, orgMap, orgLeaderMap));
            }
            export.setStoreTagNames(storeTagMap.get(complaint.getStoreId()));
            export.setComplaintImageUrls(String.join("\n",
                    complaintImageMap.getOrDefault(complaint.getId(), Collections.emptyList())));
            export.setBusinessTypeName(complaintTypeNames.getOrDefault(complaint.getBusinessType(), ""));
            export.setComplaintStatusName(complaintStatusNames.getOrDefault(complaint.getComplaintType(), ""));
            export.setAcceptFollowUpName(Objects.equals(complaint.getAcceptFollowUp(), 1) ? "是" : "否");
            return export;
        }).toList();
    }

    private Map<Long, OrgDO> loadOrganizationMap(Set<Long> orgIds) {
        if (CollectionUtil.isEmpty(orgIds)) {
            return Collections.emptyMap();
        }
        List<OrgDO> directOrgs = orgMapper.selectBatchIds(orgIds);
        Set<Long> allOrgIds = new HashSet<>(orgIds);
        for (OrgDO org : directOrgs) {
            if (!StringUtils.hasText(org.getAncestors())) {
                continue;
            }
            Arrays.stream(org.getAncestors().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf)
                    .forEach(allOrgIds::add);
        }
        return orgMapper.selectBatchIds(allOrgIds).stream()
                .collect(Collectors.toMap(OrgDO::getId, org -> org, (first, ignored) -> first));
    }

    private Map<Long, AdminUserDO> loadOrganizationLeaderMap(Set<Long> orgIds) {
        if (CollectionUtil.isEmpty(orgIds)) {
            return Collections.emptyMap();
        }
        List<OrgUserDO> leaderRelations = orgUserMapper.selectList(new LambdaQueryWrapperX<OrgUserDO>()
                .in(OrgUserDO::getOrgId, orgIds)
                .eq(OrgUserDO::getType, 1));
        Set<Long> userIds = leaderRelations.stream().map(OrgUserDO::getUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        Map<Long, AdminUserDO> userMap = adminUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(AdminUserDO::getId, user -> user, (first, ignored) -> first));
        return leaderRelations.stream()
                .filter(relation -> userMap.containsKey(relation.getUserId()))
                .collect(Collectors.toMap(OrgUserDO::getOrgId,
                        relation -> userMap.get(relation.getUserId()), (first, ignored) -> first));
    }

    private Map<Long, String> loadStoreTagMap(Set<Long> storeIds) {
        if (CollectionUtil.isEmpty(storeIds)) {
            return Collections.emptyMap();
        }
        List<SystemStoreTagDO> relations = systemStoreTagMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreTagDO>().in(SystemStoreTagDO::getStoreId, storeIds));
        Set<Long> tagIds = relations.stream().map(SystemStoreTagDO::getTagId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtil.isEmpty(tagIds)) {
            return Collections.emptyMap();
        }
        Map<Long, String> tagNameMap = tagValueMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(TagValueDO::getId, TagValueDO::getName, (first, ignored) -> first));
        return relations.stream().collect(Collectors.groupingBy(SystemStoreTagDO::getStoreId,
                Collectors.mapping(relation -> tagNameMap.get(relation.getTagId()),
                        Collectors.filtering(Objects::nonNull, Collectors.joining(",")))));
    }

    private Map<Long, List<String>> loadComplaintImageMap(List<ComplaintDO> complaints) {
        Set<Long> complaintIds = complaints.stream().map(ComplaintDO::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtil.isEmpty(complaintIds)) {
            return Collections.emptyMap();
        }
        return complaintImgMapper.selectList(new LambdaQueryWrapperX<ComplaintImgDO>()
                        .in(ComplaintImgDO::getComplaintId, complaintIds)
                        .orderByAsc(ComplaintImgDO::getId)).stream()
                .collect(Collectors.groupingBy(ComplaintImgDO::getComplaintId,
                        Collectors.mapping(ComplaintImgDO::getImgUrl, Collectors.toList())));
    }

    private String formatOrganizationAndLeader(OrgDO org,
                                                Map<Long, OrgDO> orgMap,
                                                Map<Long, AdminUserDO> orgLeaderMap) {
        if (org == null) {
            return "";
        }
        AdminUserDO leader = findOrganizationLeader(org, orgMap, orgLeaderMap);
        if (leader == null) {
            return org.getName();
        }
        String leaderInfo = Optional.ofNullable(leader.getNickname()).orElse("");
        if (StringUtils.hasText(leader.getMobile())) {
            leaderInfo += " " + leader.getMobile();
        }
        return StringUtils.hasText(leaderInfo) ? org.getName() + "/" + leaderInfo : org.getName();
    }

    private AdminUserDO findOrganizationLeader(OrgDO org,
                                                Map<Long, OrgDO> orgMap,
                                                Map<Long, AdminUserDO> orgLeaderMap) {
        Set<Long> visited = new HashSet<>();
        OrgDO current = org;
        while (current != null && visited.add(current.getId())) {
            AdminUserDO leader = orgLeaderMap.get(current.getId());
            if (leader != null) {
                return leader;
            }
            current = orgMap.get(current.getParentId());
        }
        return null;
    }

}
