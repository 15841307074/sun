package com.htyoudao.youdao.module.system.service.printer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.ServiceException;
import com.alibaba.fastjson2.TypeReference;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.printer.*;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.mysql.printer.PrinterSettingMapper;
import com.htyoudao.youdao.module.system.dal.mysql.printer.PrinterTableMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.enums.PrinterErrorCode;
import com.htyoudao.youdao.module.system.enums.PrinterTomplateTypeEnum;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.util.page.PageUtils;
import com.htyoudao.youdao.module.system.util.printer.MultiReceiptTemplate;
import com.htyoudao.youdao.module.system.util.xpyun.UpdPrinterRequest;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.string.ConvertUtil.convertListToString;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;

@Service
@Slf4j
public class PrinterTableServiceImpl extends ServiceImpl<PrinterTableMapper, PrinterTable> implements IPrinterTableService {

    @Resource
    private PrinterTableMapper printerTableMapper;

    @Resource
    private IPrinterTableService printerTableService;
    @Resource
    private IPrinterSettingService printerSettingService;


    @Resource
    private PrinterSettingMapper printerSettingMapper;
    @Resource
    private IPrinterBrandTableService printerBrandTableService;

    @Resource
    private IPrinterInterfaceService printerInterfaceService;
    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;


    /**
     * 查询门店打印机列表
     *
     * @param printerTable 门店打印机
     * @return 门店打印机
     */
    @Override
    public Page<PrinterTable> selectPrinterTableList(PrinterTable printerTable) {
        return printerTableMapper.selectPrinterTableListPage(PageUtils.getPageInfo(), printerTable);
    }

    @Override
    public List<PrinterTable> getPrinterTableList(PrinterTable printerTable) {
        return printerTableMapper.selectPrinterTableList(printerTable);
    }

    /**
     * 新增门店打印机
     *
     * @param printerTable 门店打印机
     * @return 结果
     */
    @Override
    @Transactional
    public boolean insertPrinterTable(PrinterTable printerTable) {

        Long businessId = BusinessContextHolder.getBusinessId();

        if (printerTable != null) {
            String printerName = printerTable.getPrinterName();
            LambdaQueryWrapper<PrinterTable> printerTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
            printerTableLambdaQueryWrapper.eq(PrinterTable::getPrinterName, printerName);
            if(!org.springframework.util.StringUtils.isEmpty(printerTable.getStoreId())){
                printerTableLambdaQueryWrapper.eq(PrinterTable::getStoreId, printerTable.getStoreId());
            }
            printerTable.setStatus(0);
            printerTableLambdaQueryWrapper.eq(PrinterTable::getBusinessId,businessId);
            List<PrinterTable> printerTables = printerTableMapper.selectList(printerTableLambdaQueryWrapper);
            if (printerTables.size() > 0) {
                throw exception(SYSTEM_PRINTER_NAME_FAIL);
            }

        }
        if (printerTable.getPrinterType() == 1) {
            //新增打印机
            List<Long> printerSettingIds = new ArrayList<>();

            //如果打印机设置内容不是空
            if (!printerTable.getPrinterSettingList().isEmpty()) {
                //遍历打印机设置
                for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                    //新增打印机设置
                    printerSettingMapper.insert(printerSetting);
                    //然后把 id 添加到集合里
                    printerSettingIds.add(printerSetting.getPrinterSettingId());
                }
                //将打印机设置的 id 集合存到打印机表里
                printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
            }
            printerTable.setBusinessId(businessId);
            printerTableMapper.insert(printerTable);

            return true;
        } else {
            if(!org.springframework.util.StringUtils.isEmpty(printerTable.getPrinterSerialNumber())){
                LambdaQueryWrapper<PrinterTable> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PrinterTable::getPrinterSerialNumber, printerTable.getPrinterSerialNumber());
                wrapper.eq(PrinterTable::getPrinterType, 2);
//                wrapper.eq(PrinterTable::getStoreId, printerTable.getStoreId());
                List<PrinterTable> printerTables1 = printerTableMapper.selectList(wrapper);
                if (printerTables1.size() > 0) {
                    PrinterTable printerTable2 = printerTables1.get(0);
                    if(!org.springframework.util.StringUtils.isEmpty(printerTable2.getStoreId())){
                        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(printerTable2.getStoreId());
                        if(systemStoreInfoDO!=null){
                            throw exception(1_006_000_010,"该打印机已在"+systemStoreInfoDO.getStoreName()+"门店绑定");
                        }
                    }
                }
            }

            //首先拿到品牌名称
            String printerBrand = printerTable.getPrinterBrand();
            //初始化打印机表
            PrinterBrandTable printerBrandTable = new PrinterBrandTable();
            //将打印机品牌赋值
            printerBrandTable.setPrinterBrandName(printerBrand);
            //查询出该品牌下的打印机品牌信息
            PrinterBrandTable printerBrandTables = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);
            //开始查询此打印机功能方式
            //初始化功能对象
            PrinterInterface printerInterface = new PrinterInterface();
            //设置打印机品牌 id
            printerInterface.setPrinterBrandId(printerBrandTables.getPrinterBrandId());
            //设置打印机功能编号
            printerInterface.setPrinterInterfaceAbility(1L);
            //拿到功能内容
            PrinterInterface printerInterfaces = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);

            switch (printerBrand) {
                case "芯烨": {
                    log.info(">>> 添加芯烨打印机");
                    PrinterTable printerTable1 = new PrinterTable();

                    printerTable1.setPrinterBrand(printerBrand);
                    printerTable1.setPrinterSerialNumber(printerTable.getPrinterSerialNumber());
                    printerTable1.setPrinterType(2);
                    List<PrinterTable> printerTables = printerTableMapper.selectPrinterTableList(printerTable1);

                    if (printerTables.isEmpty()) {
                        // 添加公共参数
                        AddPrinterRequest request = new AddPrinterRequest();
                        //*必填*：芯烨云平台注册用户名（开发者 ID）
                        request.setUser(printerBrandTables.getPrinterBrandUser());
                        //*必填*：当前UNIX时间戳
                        request.setTimestamp(System.currentTimeMillis() + "");
                        //*必填*：对参数 user + UserKEY + timestamp 拼接后（+号表示连接符）进行SHA1加密得到签名，值为40位小写字符串，其中 UserKEY 为用户开发者密钥/
                        request.setSign(HashSignUtil.sign(request.getUser() + printerBrandTables.getPrinterBrandUserkey() + request.getTimestamp()));
                        //debug=1返回非json格式的数据，仅测试时候使用
                        request.setDebug("0");
                        //打印机列表
                        List<AddPrinterRequestItem> itemList = new ArrayList<>();
                        AddPrinterRequestItem item = new AddPrinterRequestItem();
                        item.setSn(printerTable.getPrinterSerialNumber());
                        item.setName(printerTable.getPrinterName());
                        itemList.add(item);
                        //*必填*：items:数组元素为 json 对象：
                        //{"name":"打印机名称","sn":"打印机编号"}
                        //其中打印机编号 sn 和名称 name 字段为必填项，每次最多添加50台
                        AddPrinterRequestItem[] items = new AddPrinterRequestItem[itemList.size()];
                        itemList.toArray(items);
                        request.setItems(items);
                        String url = printerBrandTables.getPrinterBrandInterface() + printerInterfaces.getPrinterInterfaceValue();
                        String jsonRequest = JSON.toJSONString(request);
                        String resp = HttpClientUtil.doPostJSON(url, jsonRequest);
                        ObjectRestResponse<PrinterResult> result = JSON.parseObject(resp, new TypeReference<ObjectRestResponse<PrinterResult>>() {
                        });
//                        log.info("添加芯烨打印机返回信息"+result.getMsg());
//                        log.info("添加芯烨打印机返回信息"+result.getData().getSuccess().toString());
                        //resp.data:返回1个 json 对象，包含成功和失败的信息，详看https://www.xpyun.net/open/index.html示例
                        if(ObjectUtil.isNotEmpty(result.getData())){
                            List<String> success1 = result.getData().getSuccess();
                            if (!success1.isEmpty()) {
                                //新增打印机
                                List<Long> printerSettingIds = new ArrayList<>();
                                //如果打印机设置内容不是空
                                if (!printerTable.getPrinterSettingList().isEmpty()) {
                                    //遍历打印机设置
                                    for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                                        //新增打印机设置
//                                    printerSetting.setProjectOwnerShip(projectOwnerShip);
                                        printerSettingService.insertPrinterSetting(printerSetting);
                                        //然后把 id 添加到集合里
                                        printerSettingIds.add(printerSetting.getPrinterSettingId());
                                    }
                                    //将打印机设置的 id 集合存到打印机表里
                                    printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
                                }
                                //新增打印机
                                printerTable.setBusinessId(businessId);
                                printerTableMapper.insertPrinterTable(printerTable);
                                return true;
                            } else {
                                String string = result.getData().getFailMsg().toString();
                                String cleaned = string.replace("[", "").replace("]", "");
                                String[] parts = cleaned.split(":");

                                if (parts.length > 1) {
                                    String valueAfterColon = parts[1];
// 将字符串转换为整数
                                    int errorCode = Integer.parseInt(valueAfterColon);

// 通过错误码获取枚举实例
                                    PrinterErrorCode printerError = PrinterErrorCode.getByCode(errorCode);
                                    if (printerError != null) {
                                        String errorMessage = printerError.getMessage();
                                        printerTableMapper.deletePrinterTableByPrinterTableId(printerTable.getPrinterTableId());
                                        // 抛出异常或进行其他处理
                                        throw exception(1_006_000_030, errorMessage);
                                    }else{
                                        throw exception(PRINTER_NUMBER_INVALID);
                                    }
                                }else{
                                    printerTableMapper.deletePrinterTableByPrinterTableId(printerTable.getPrinterTableId());
                                    throw exception(SYSTEM_PRINTER_REGISTER_ERRORS);
                                }



                            }
                        }else{
                            throw exception(SYSTEM_PRINTER_REGISTER);
                        }

                    } else {
                        PrinterTable printerTable2 = printerTables.get(0);
                        printerTable2.setPrinterName(printerTable.getPrinterName());
                        printerTable2.setStoreId(printerTable.getStoreId());
                        if (!printerTable.getPrinterSettingList().isEmpty()) {
                            List<Long> setIds = new ArrayList<>();
                            for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
//                                printerSetting.setProjectOwnerShip(projectOwnerShip);
                                printerSettingService.insertPrinterSetting(printerSetting);
                                setIds.add(printerSetting.getPrinterSettingId());
                            }
                            printerTable2.setPrinterSettingIds(convertListToString(setIds));
                        }
                        printerTableMapper.updatePrinterTable(printerTable2);
                        return true;
                    }


                }
                case "飞鹅": {
                    log.info(">>> 添加飞鹅打印机");
                    PrinterTable printerTable1 = new PrinterTable();
                    printerTable1.setPrinterBrand(printerBrand);
                    printerTable1.setPrinterSerialNumber(printerTable.getPrinterSerialNumber());
                    List<PrinterTable> printerTables = printerTableMapper.selectPrinterTableList(printerTable1);
                    if (printerTables.isEmpty()) {
                        RequestConfig requestConfig = RequestConfig.custom()
                                .setSocketTimeout(30000)//读取超时
                                .setConnectTimeout(30000)//连接超时
                                .build();
                        CloseableHttpClient httpClient = HttpClients.custom()
                                .setDefaultRequestConfig(requestConfig)
                                .build();
                        HttpPost post = new HttpPost(printerBrandTables.getPrinterBrandInterface());
                        String savePrinterContent = printerTable.getPrinterSerialNumber() + "#" + printerTable.getPrinterSerialKey() + "#" + printerTable.getPrinterName();
                        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                        nvps.add(new BasicNameValuePair("user", printerBrandTables.getPrinterBrandUser()));
                        String STIME = String.valueOf(System.currentTimeMillis() / 1000);
                        nvps.add(new BasicNameValuePair("stime", STIME));
                        nvps.add(new BasicNameValuePair("sig", signature(printerBrandTables.getPrinterBrandUser(), printerBrandTables.getPrinterBrandUserkey(), STIME)));
                        nvps.add(new BasicNameValuePair("apiname", printerInterfaces.getPrinterInterfaceValue()));
                        nvps.add(new BasicNameValuePair("printerContent", savePrinterContent));
                        CloseableHttpResponse response = null;
                        String result;
                        JSONArray no = new JSONArray();
                        try {
                            post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
                            response = httpClient.execute(post);
                            int statecode = response.getStatusLine().getStatusCode();
                            log.info(">>> 添加飞鹅打印机 接口调用状态返回" + statecode);
                            if (statecode == 200) {
                                HttpEntity httpentity = response.getEntity();
                                result = EntityUtils.toString(httpentity);
                                JSONObject jsonObject = JSONObject.parseObject(result);
                                log.info(">>> 添加飞鹅打印机 接口返回信息 {}", jsonObject);
                                if (!"ok".equals(jsonObject.get("msg").toString())) {

                                    throw new ServiceException(jsonObject.get("msg").toString());
                                }
                                JSONObject data = (JSONObject) jsonObject.get("data");
                                no = JSONArray.parseArray(JSON.toJSONString(data.get("no")));

                            } else {
                                throw exception(SYSTEM_INTERFACE_ERROR);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (response != null) {
                                    response.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                post.abort();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                httpClient.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (no.size() > 0) {
                           String errorString = no.get(0).toString();
                            int start = errorString.indexOf("（") + 1;
                            int end = errorString.indexOf("）");
                            String errorMessage = (start != -1 && end != -1) ? errorString.substring(start, end) : "";
                            if (errorMessage != null && errorMessage.contains("不能为空")) {
                                throw exception(SYSTEM_PRINTER_HEFA);
                            } else {
                                throw exception(1_006_000_020,errorMessage);
                            }


                        }
                        //新增打印机
                        List<Long> printerSettingIds = new ArrayList<>();
                        //如果打印机设置内容不是空
                        if (!printerTable.getPrinterSettingList().isEmpty()) {
                            //遍历打印机设置
                            for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                                //新增打印机设置
//                                printerSetting.setProjectOwnerShip(projectOwnerShip);
                                printerSettingMapper.insert(printerSetting);
                                //然后把 id 添加到集合里
                                printerSettingIds.add(printerSetting.getPrinterSettingId());
                            }
                            //将打印机设置的 id 集合存到打印机表里
                            printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
                        }
                        //新增打印机
//                        printerTable.setProjectOwnerShip(projectOwnerShip);
                        printerTable.setBusinessId(businessId);
                        printerTableMapper.insert(printerTable);
                        return true;
                        /*assert result != null;
                        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
                        JsonObject data = jsonResult.getAsJsonObject("data");
                        try {
                            JsonArray okArray = data.getAsJsonArray("ok");
                            if (okArray.get(0).getAsString().equals(savePrinterContent)){

                            }
                        } catch (Exception e){
                            printerTableMapper.deletePrinterTableByPrinterTableId(printerTable.getPrinterTableId());
                            return  AjaxResult.error("发生未知异常，请查看序列码和网络");
                        }*/
                    } else {
                        PrinterTable printerTable2 = new PrinterTable();
                        printerTable2.setPrinterBrand(printerTable.getPrinterBrand());
                        printerTable2.setPrinterSerialNumber(printerTable.getPrinterSerialNumber());
                        List<PrinterTable> printerTables1 = printerTableMapper.selectPrinterTableList(printerTable2);
                        if (!printerTables1.isEmpty()) {
                            PrinterTable printerTable3 = printerTables1.get(0);
                            printerTable3.setPrinterName(printerTable.getPrinterName());
                            printerTable3.setStoreId(printerTable.getStoreId());
                            if (!printerTable.getPrinterSettingList().isEmpty()) {
                                List<Long> printerSettingIds1 = new ArrayList<>();
                                for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                                    //新增打印机设置
//                                    printerSetting.setProjectOwnerShip(projectOwnerShip);
                                    printerSettingMapper.insert(printerSetting);
                                    //然后把 id 添加到集合里
                                    printerSettingIds1.add(printerSetting.getPrinterSettingId());
                                }
                                printerTable3.setPrinterSettingIds(convertListToString(printerSettingIds1));
                            }
                            printerTableMapper.updatePrinterTable(printerTable3);
                        }
                    }
                    return true;
                }
            }
        }

        return true;
    }
    /**
     * 修改门店打印机
     *
     * @param printerTable 门店打印机
     * @return 结果
     */
    @Override
    public Boolean updatePrinterTable(PrinterTable printerTable) {
        Long businessId = BusinessContextHolder.getBusinessId();
        PrinterTable printerTable1 = printerTableMapper.selectPrinterTableByPrinterTableId(printerTable.getPrinterTableId());
        if(printerTable1==null){
            return false;
        }
        if (printerTable.getPrinterType() == 1 & printerTable1.getPrinterType() == 1) {
            if(!printerTable.getPrinterName().equals(printerTable1.getPrinterName())){
                String printerName = printerTable.getPrinterName();
                LambdaQueryWrapper<PrinterTable> printerTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
                printerTableLambdaQueryWrapper.eq(PrinterTable::getPrinterName, printerName);
                if(!org.springframework.util.StringUtils.isEmpty(printerTable.getStoreId())){
                    printerTableLambdaQueryWrapper.eq(PrinterTable::getStoreId, printerTable.getStoreId());
                }
                printerTableLambdaQueryWrapper.eq(PrinterTable::getBusinessId,businessId);
                List<PrinterTable> printerTables = printerTableMapper.selectList(printerTableLambdaQueryWrapper);
                if (printerTables.size() > 0) {
                    throw exception(SYSTEM_PRINTER_NAME_FAIL);
                }
            }
            //初始化打印机设置id 集合
            List<Long> printerSettingIds = new ArrayList<>();
            //如果打印机的设置列表不是空
            if (!printerTable.getPrinterSettingList().isEmpty()) {
                //遍历打印机设置列表
                for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                    //如果打印机设置的 id 不是空 说明是修改打印机设置
                    if (printerSetting.getPrinterSettingId() != null) {
                        //那就走修改方法
                        printerSettingService.updatePrinterSetting(printerSetting);
                        //将打印机的 id 拿出来放到打印机 id集合里去
                        printerSettingIds.add(printerSetting.getPrinterSettingId());
                    } else {
                        //如果修改的打印机设置没有 id 说名是新增打印机设置
                        printerSettingService.insertPrinterSetting(printerSetting);
                        //然后还是把 id 添加到集合里去
                        printerSettingIds.add(printerSetting.getPrinterSettingId());
                    }
                }
                //然后把集合变成字符串存到打印机 ids 的字段里去
                printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
            }else{
                printerTable.setPrinterSettingIds(null);
            }
            printerTableMapper.updatePrinterTable(printerTable);
            return true;
        } else if ((printerTable.getPrinterType() == 1 & printerTable1.getPrinterType() == 2)) {
            if(!printerTable.getPrinterName().equals(printerTable1.getPrinterName())){
                String printerName = printerTable.getPrinterName();
                LambdaQueryWrapper<PrinterTable> printerTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
                printerTableLambdaQueryWrapper.eq(PrinterTable::getPrinterName, printerName);
                if(!org.springframework.util.StringUtils.isEmpty(printerTable.getStoreId())){
                    printerTableLambdaQueryWrapper.eq(PrinterTable::getStoreId, printerTable.getStoreId());
                }
                printerTableLambdaQueryWrapper.eq(PrinterTable::getBusinessId,businessId);
                List<PrinterTable> printerTables = printerTableMapper.selectList(printerTableLambdaQueryWrapper);
                if (printerTables.size() > 0) {
                    throw exception(SYSTEM_PRINTER_NAME_FAIL);
                }
            }
            List<Long> printerSettingIds = new ArrayList<>();
            //如果打印机的设置列表不是空
            if (!printerTable.getPrinterSettingList().isEmpty()) {
                //遍历打印机设置列表
                for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                    //如果打印机设置的 id 不是空 说明是修改打印机设置
                    if (printerSetting.getPrinterSettingId() != null) {
                        //那就走修改方法
                        printerSettingService.updatePrinterSetting(printerSetting);
                        //将打印机的 id 拿出来放到打印机 id集合里去
                        printerSettingIds.add(printerSetting.getPrinterSettingId());
                    } else {
                        //如果修改的打印机设置没有 id 说名是新增打印机设置
                        printerSettingService.insertPrinterSetting(printerSetting);
                        //然后还是把 id 添加到集合里去
                        printerSettingIds.add(printerSetting.getPrinterSettingId());
                    }
                }
                //然后把集合变成字符串存到打印机 ids 的字段里去
                printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
            }else{
                printerTable.setPrinterSettingIds(null);
            }
            printerTableMapper.updatePrinterTable(printerTable);
            switch (printerTable1.getPrinterBrand()) {
                case "芯烨": {
                    DelPrinterRequest request = new DelPrinterRequest();
                    //*必填*：打印机编号集合，类型为字符串数组
                    String[] snlist = new String[1];
                    //*必填*：打印机编号
                    snlist[0] = printerTable1.getPrinterSerialNumber();
                    PrinterBrandTable printerBrandTable = new PrinterBrandTable();
                    printerBrandTable.setPrinterBrandName(printerTable1.getPrinterBrand());
                    PrinterBrandTable printerBrandTable1 = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);

                    request.setUser(printerBrandTable1.getPrinterBrandUser());
                    request.setTimestamp(System.currentTimeMillis() + "");
                    //*必填*：对参数 user + UserKEY + timestamp 拼接后（+号表示连接符）进行SHA1加密得到签名，值为40位小写字符串，其中 UserKEY 为用户开发者密钥
                    request.setSign(HashSignUtil.sign(request.getUser() + printerBrandTable1.getPrinterBrandUserkey() + request.getTimestamp()));
                    request.setSnlist(snlist);
                    PrinterInterface printerInterface = new PrinterInterface();
                    printerInterface.setPrinterBrandId(printerBrandTable1.getPrinterBrandId());
                    printerInterface.setPrinterInterfaceAbility(4L);
                    PrinterInterface printerInterface1 = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);
                    String url = printerBrandTable1.getPrinterBrandInterface() + printerInterface1.getPrinterInterfaceValue();
                    String jsonRequest = JSON.toJSONString(request);
                    String resp = HttpClientUtil.doPostJSON(url, jsonRequest);
                    ObjectRestResponse<PrinterResult> result = JSON.parseObject(resp, new TypeReference<ObjectRestResponse<PrinterResult>>() {
                    });
                    if (Objects.equals(result.getMsg(), "ok")) {
                        return true;
                    } else {
                        throw exception(SYSTEM_PRINTER_DELETE_FAIL);
                    }
                }
                case "飞鹅": {
                    //通过POST请求，发送打印信息到服务器
                    RequestConfig requestConfig = RequestConfig.custom()
                            .setSocketTimeout(30000)//读取超时
                            .setConnectTimeout(30000)//连接超时
                            .build();
                    CloseableHttpClient httpClient = HttpClients.custom()
                            .setDefaultRequestConfig(requestConfig)
                            .build();
                    PrinterBrandTable printerBrandTable = new PrinterBrandTable();
                    printerBrandTable.setPrinterBrandName(printerTable1.getPrinterBrand());
                    PrinterBrandTable printerBrandTable1 = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);
                    PrinterInterface printerInterface = new PrinterInterface();
                    printerInterface.setPrinterInterfaceAbility(4L);
                    printerInterface.setPrinterBrandId(printerBrandTable1.getPrinterBrandId());
                    PrinterInterface printerInterface1 = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);
                    HttpPost post = new HttpPost(printerBrandTable1.getPrinterBrandInterface());
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                    nvps.add(new BasicNameValuePair("user", printerBrandTable1.getPrinterBrandUser()));
                    String STIME = String.valueOf(System.currentTimeMillis() / 1000);
                    nvps.add(new BasicNameValuePair("stime", STIME));
                    nvps.add(new BasicNameValuePair("sig", signature(printerBrandTable1.getPrinterBrandUser(), printerBrandTable1.getPrinterBrandUserkey(), STIME)));
                    nvps.add(new BasicNameValuePair("apiname", printerInterface1.getPrinterInterfaceValue()));
                    nvps.add(new BasicNameValuePair("snlist", printerTable1.getPrinterSerialNumber()));
                    CloseableHttpResponse response = null;
                    String result = null;
                    try {
                        post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
                        response = httpClient.execute(post);
                        int statecode = response.getStatusLine().getStatusCode();
                        if (statecode == 200) {
                            HttpEntity httpentity = response.getEntity();
                            if (httpentity != null) {
                                result = EntityUtils.toString(httpentity);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (response != null) {
                                response.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            post.abort();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            httpClient.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    return true;

                }
            }

        } else if (printerTable.getPrinterType() == 2 & printerTable1.getPrinterType() == 1) {
            if(!printerTable.getPrinterName().equals(printerTable1.getPrinterName())){
                String printerName = printerTable.getPrinterName();
                LambdaQueryWrapper<PrinterTable> printerTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
                printerTableLambdaQueryWrapper.eq(PrinterTable::getPrinterName, printerName);
                if(!org.springframework.util.StringUtils.isEmpty(printerTable.getStoreId())){
                    printerTableLambdaQueryWrapper.eq(PrinterTable::getStoreId, printerTable.getStoreId());
                }
                printerTableLambdaQueryWrapper.eq(PrinterTable::getBusinessId,businessId);
                List<PrinterTable> printerTables = printerTableMapper.selectList(printerTableLambdaQueryWrapper);
                if (printerTables.size() > 0) {
                    throw exception(SYSTEM_PRINTER_NAME_FAIL);
                }
            }

            if(!org.springframework.util.StringUtils.isEmpty(printerTable.getPrinterSerialNumber())){
                LambdaQueryWrapper<PrinterTable> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PrinterTable::getPrinterSerialNumber, printerTable.getPrinterSerialNumber());
                wrapper.eq(PrinterTable::getPrinterType, 2);
//                wrapper.eq(PrinterTable::getStoreId, printerTable.getStoreId());
                List<PrinterTable> printerTablesOne = printerTableMapper.selectList(wrapper);
                if (printerTablesOne.size() > 0) {
                    PrinterTable printerTable2 = printerTablesOne.get(0);
                    if(!org.springframework.util.StringUtils.isEmpty(printerTable2.getStoreId())){
                        SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(printerTable2.getStoreId());
                        if(systemStoreInfoDO!=null){
                            throw exception(1_006_000_010,"该打印机已在"+systemStoreInfoDO.getStoreName()+"门店绑定");
                        }
                    }
                }
            }
            //首先拿到品牌名称
            String printerBrand = printerTable.getPrinterBrand();
            //初始化打印机表
            PrinterBrandTable printerBrandTable = new PrinterBrandTable();
            //将打印机品牌赋值
            printerBrandTable.setPrinterBrandName(printerBrand);
            //查询出该品牌下的打印机品牌信息
            PrinterBrandTable printerBrandTables = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);
            //开始查询此打印机功能方式
            //初始化功能对象
            PrinterInterface printerInterface = new PrinterInterface();
            //设置打印机品牌 id
            printerInterface.setPrinterBrandId(printerBrandTables.getPrinterBrandId());
            //设置打印机功能编号
            printerInterface.setPrinterInterfaceAbility(1L);
            //拿到功能内容
            PrinterInterface printerInterfaces = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);
            switch (printerBrand) {
                case "芯烨": {
                    log.info(">>> 添加芯烨打印机");
                    PrinterTable printerTable2 = new PrinterTable();

                    printerTable2.setPrinterBrand(printerBrand);
                    printerTable2.setPrinterSerialNumber(printerTable.getPrinterSerialNumber());
                    printerTable2.setPrinterType(2);
                    List<PrinterTable> printerTables = printerTableMapper.selectPrinterTableList(printerTable2);

                    if (printerTables.isEmpty()) {
                        // 添加公共参数
                        AddPrinterRequest request = new AddPrinterRequest();
                        //*必填*：芯烨云平台注册用户名（开发者 ID）
                        request.setUser(printerBrandTables.getPrinterBrandUser());
                        //*必填*：当前UNIX时间戳
                        request.setTimestamp(System.currentTimeMillis() + "");
                        //*必填*：对参数 user + UserKEY + timestamp 拼接后（+号表示连接符）进行SHA1加密得到签名，值为40位小写字符串，其中 UserKEY 为用户开发者密钥/
                        request.setSign(HashSignUtil.sign(request.getUser() + printerBrandTables.getPrinterBrandUserkey() + request.getTimestamp()));
                        //debug=1返回非json格式的数据，仅测试时候使用
                        request.setDebug("0");
                        List<AddPrinterRequestItem> itemList = new ArrayList<>();
                        AddPrinterRequestItem item = new AddPrinterRequestItem();
                        item.setSn(printerTable.getPrinterSerialNumber());
                        item.setName(printerTable.getPrinterName());
                        itemList.add(item);
                        //*必填*：items:数组元素为 json 对象：
                        //{"name":"打印机名称","sn":"打印机编号"}
                        //其中打印机编号 sn 和名称 name 字段为必填项，每次最多添加50台
                        AddPrinterRequestItem[] items = new AddPrinterRequestItem[itemList.size()];
                        itemList.toArray(items);
                        request.setItems(items);
                        String url = printerBrandTables.getPrinterBrandInterface() + printerInterfaces.getPrinterInterfaceValue();
                        String jsonRequest = JSON.toJSONString(request);
                        String resp = HttpClientUtil.doPostJSON(url, jsonRequest);
                        ObjectRestResponse<PrinterResult> result = JSON.parseObject(resp, new TypeReference<ObjectRestResponse<PrinterResult>>() {
                        });
                        PrinterResult data = result.getData();
                        log.info("添加芯烨打印机返回信息"+result.getMsg());
                        log.info("添加芯烨打印机返回信息"+result.getData().getSuccess().toString());
                        //resp.data:返回1个 json 对象，包含成功和失败的信息，详看https://www.xpyun.net/open/index.html示例
                        List<String> success1 = result.getData().getSuccess();
                        if (!success1.isEmpty()) {
                            //新增打印机
                            List<Long> printerSettingIds = new ArrayList<>();
                            //如果打印机设置内容不是空
                            if (!printerTable.getPrinterSettingList().isEmpty()) {
                                //遍历打印机设置
                                for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                                    //新增打印机设置
//                                    printerSetting.setProjectOwnerShip(projectOwnerShip);
                                    printerSettingService.insertPrinterSetting(printerSetting);
                                    //然后把 id 添加到集合里
                                    printerSettingIds.add(printerSetting.getPrinterSettingId());
                                }
                                //将打印机设置的 id 集合存到打印机表里
                                printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
                            }else{
                                printerTable.setPrinterSettingIds(null);
                            }
                            //新增打印机
                            printerTable.setBusinessId(businessId);
                            printerTableMapper.updatePrinterTable(printerTable);
                            return true;
                        }
                    }
                }
                case "飞鹅": {
                    log.info(">>> 添加飞鹅打印机");
                    PrinterTable printerTable2 = new PrinterTable();
                    printerTable2.setPrinterBrand(printerBrand);
                    printerTable2.setPrinterSerialNumber(printerTable.getPrinterSerialNumber());
                    printerTable2.setPrinterType(2);
                    List<PrinterTable> printerTables = printerTableMapper.selectPrinterTableList(printerTable2);
                    if (printerTables.isEmpty()) {
                        RequestConfig requestConfig = RequestConfig.custom()
                                .setSocketTimeout(30000)//读取超时
                                .setConnectTimeout(30000)//连接超时
                                .build();
                        CloseableHttpClient httpClient = HttpClients.custom()
                                .setDefaultRequestConfig(requestConfig)
                                .build();
                        HttpPost post = new HttpPost(printerBrandTables.getPrinterBrandInterface());
                        String savePrinterContent = printerTable.getPrinterSerialNumber() + "#" + printerTable.getPrinterSerialKey() + "#" + printerTable.getPrinterName();
                        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                        nvps.add(new BasicNameValuePair("user", printerBrandTables.getPrinterBrandUser()));
                        String STIME = String.valueOf(System.currentTimeMillis() / 1000);
                        nvps.add(new BasicNameValuePair("stime", STIME));
                        nvps.add(new BasicNameValuePair("sig", signature(printerBrandTables.getPrinterBrandUser(), printerBrandTables.getPrinterBrandUserkey(), STIME)));
                        nvps.add(new BasicNameValuePair("apiname", printerInterfaces.getPrinterInterfaceValue()));
                        nvps.add(new BasicNameValuePair("printerContent", savePrinterContent));
                        CloseableHttpResponse response = null;
                        String result;
                        try {
                            post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
                            response = httpClient.execute(post);
                            int statecode = response.getStatusLine().getStatusCode();
                            log.info(">>> 添加飞鹅打印机 接口调用状态返回" + statecode);
                            if (statecode == 200) {
                                HttpEntity httpentity = response.getEntity();
                                result = EntityUtils.toString(httpentity);
                                JSONObject jsonObject = JSONObject.parseObject(result);
                                //log.info(">>> 添加飞鹅打印机 接口返回信息 {}", jsonObject);
                                if (!"ok".equals(jsonObject.get("msg").toString())) {

                                    throw new ServiceException(jsonObject.get("msg").toString());
                                }
                                JSONObject data = (JSONObject) jsonObject.get("data");
                                JSONArray no = JSONArray.parseArray(JSON.toJSONString(data.get("no")));
                                if (no.size() > 0) {
                                    Object o = no.get(0).toString();
                                    throw exception(SYSTEM_PRINTER_GESHI_ERROR);
                                }
                            } else {
                                throw exception(SYSTEM_PRINTER_REGISTER_ERROR);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw exception(SYSTEM_PRINTER_INSERT_ERROR);
                        } finally {
                            try {
                                if (response != null) {
                                    response.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                post.abort();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                httpClient.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //新增打印机
                        List<Long> printerSettingIds = new ArrayList<>();
                        //如果打印机设置内容不是空
                        if (!printerTable.getPrinterSettingList().isEmpty()) {
                            //遍历打印机设置
                            for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                                //新增打印机设置
//                                printerSetting.setProjectOwnerShip(projectOwnerShip);
                                printerSettingMapper.insert(printerSetting);
                                //然后把 id 添加到集合里
                                printerSettingIds.add(printerSetting.getPrinterSettingId());
                            }
                            //将打印机设置的 id 集合存到打印机表里
                            printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
                        }else {
                            printerTable.setPrinterSettingIds(null);
                        }

                        printerTable.setBusinessId(businessId);
                        printerTableMapper.updatePrinterTable(printerTable);
                        return true;
                    }
                    return true;
                }
            }
        } else {
            if(!printerTable.getPrinterName().equals(printerTable1.getPrinterName())){
                String printerName = printerTable.getPrinterName();
                LambdaQueryWrapper<PrinterTable> printerTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
                printerTableLambdaQueryWrapper.eq(PrinterTable::getPrinterName, printerName);
                if(!org.springframework.util.StringUtils.isEmpty(printerTable.getStoreId())){
                    printerTableLambdaQueryWrapper.eq(PrinterTable::getStoreId, printerTable.getStoreId());
                }
                printerTableLambdaQueryWrapper.eq(PrinterTable::getBusinessId,businessId);
                List<PrinterTable> printerTables = printerTableMapper.selectList(printerTableLambdaQueryWrapper);
                if (printerTables.size() > 0) {
                    throw exception(SYSTEM_PRINTER_NAME_FAIL);
                }
            }
            if (!printerTable1.getPrinterSerialNumber().equals(printerTable.getPrinterSerialNumber())) {

                if(printerTable.getPrinterType().equals(2)){
                    if(!org.springframework.util.StringUtils.isEmpty(printerTable.getPrinterSerialNumber())){
                        LambdaQueryWrapper<PrinterTable> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(PrinterTable::getPrinterSerialNumber, printerTable.getPrinterSerialNumber());
                        wrapper.eq(PrinterTable::getPrinterType, 2);
//                wrapper.eq(PrinterTable::getStoreId, printerTable.getStoreId());
                        List<PrinterTable> printerTables1 = printerTableMapper.selectList(wrapper);
                        if (printerTables1.size() > 0) {
                            PrinterTable printerTable2 = printerTables1.get(0);
                            if(!org.springframework.util.StringUtils.isEmpty(printerTable2.getStoreId())){
                                SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectById(printerTable2.getStoreId());
                                if(systemStoreInfoDO!=null){
                                    throw exception(1_006_000_010,"该打印机已在"+systemStoreInfoDO.getStoreName()+"门店绑定");
                                }
                            }
                        }
                    }
                }
                //拿到打印机品牌名称
                String printerBrand = printerTable.getPrinterBrand();
                switch (printerBrand) {
                    case "芯烨": {
                        DelPrinterRequest request = new DelPrinterRequest();
                        //*必填*：打印机编号集合，类型为字符串数组
                        String[] snlist = new String[1];
                        //*必填*：打印机编号
                        snlist[0] = printerTable1.getPrinterSerialNumber();
                        PrinterBrandTable printerBrandTable = new PrinterBrandTable();
                        printerBrandTable.setPrinterBrandName(printerTable1.getPrinterBrand());
                        PrinterBrandTable printerBrandTable1 = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);

                        request.setUser(printerBrandTable1.getPrinterBrandUser());
                        request.setTimestamp(System.currentTimeMillis() + "");
                        //*必填*：对参数 user + UserKEY + timestamp 拼接后（+号表示连接符）进行SHA1加密得到签名，值为40位小写字符串，其中 UserKEY 为用户开发者密钥
                        request.setSign(HashSignUtil.sign(request.getUser() + printerBrandTable1.getPrinterBrandUserkey() + request.getTimestamp()));
                        request.setSnlist(snlist);
                        PrinterInterface printerInterface = new PrinterInterface();
                        printerInterface.setPrinterBrandId(printerBrandTable1.getPrinterBrandId());
                        printerInterface.setPrinterInterfaceAbility(4L);
                        PrinterInterface printerInterface1 = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);
                        String url = printerBrandTable1.getPrinterBrandInterface() + printerInterface1.getPrinterInterfaceValue();
                        String jsonRequest = JSON.toJSONString(request);
                        String resp = HttpClientUtil.doPostJSON(url, jsonRequest);
                        ObjectRestResponse<PrinterResult> result = JSON.parseObject(resp, new TypeReference<ObjectRestResponse<PrinterResult>>() {
                        });
                        if (Objects.equals(result.getMsg(), "ok")) {

                            //将打印机品牌赋值
                            printerBrandTable.setPrinterBrandName(printerBrand);
                            //查询出该品牌下的打印机品牌信息
                            PrinterBrandTable printerBrandTables = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);
                            //开始查询此打印机功能方式
                            //设置打印机品牌 id
                            printerInterface.setPrinterBrandId(printerBrandTables.getPrinterBrandId());
                            //设置打印机功能编号
                            printerInterface.setPrinterInterfaceAbility(1L);
                            //拿到功能内容
                            PrinterInterface printerInterfaces = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);

                            // 添加公共参数
                            AddPrinterRequest requestTwo = new AddPrinterRequest();
                            //*必填*：芯烨云平台注册用户名（开发者 ID）
                            requestTwo.setUser(printerBrandTables.getPrinterBrandUser());
                            //*必填*：当前UNIX时间戳
                            requestTwo.setTimestamp(System.currentTimeMillis() + "");
                            //*必填*：对参数 user + UserKEY + timestamp 拼接后（+号表示连接符）进行SHA1加密得到签名，值为40位小写字符串，其中 UserKEY 为用户开发者密钥/
                            requestTwo.setSign(HashSignUtil.sign(requestTwo.getUser() + printerBrandTables.getPrinterBrandUserkey() + requestTwo.getTimestamp()));
                            //debug=1返回非json格式的数据，仅测试时候使用
                            requestTwo.setDebug("0");
                            List<AddPrinterRequestItem> itemList = new ArrayList<>();
                            AddPrinterRequestItem item = new AddPrinterRequestItem();
                            item.setSn(printerTable.getPrinterSerialNumber());
                            item.setName(printerTable.getPrinterName());
                            itemList.add(item);
                            //*必填*：items:数组元素为 json 对象：
                            //{"name":"打印机名称","sn":"打印机编号"}
                            //其中打印机编号 sn 和名称 name 字段为必填项，每次最多添加50台
                            AddPrinterRequestItem[] items = new AddPrinterRequestItem[itemList.size()];
                            itemList.toArray(items);
                            requestTwo.setItems(items);
                            String urlTwo = printerBrandTables.getPrinterBrandInterface() + printerInterfaces.getPrinterInterfaceValue();
                            String jsonRequestTwo = JSON.toJSONString(requestTwo);
                            String respTwo = HttpClientUtil.doPostJSON(urlTwo, jsonRequestTwo);
                            ObjectRestResponse<PrinterResult> resultData = JSON.parseObject(respTwo, new TypeReference<ObjectRestResponse<PrinterResult>>() {
                            });
                            PrinterResult data = resultData.getData();
                            log.info("添加芯烨打印机返回信息"+resultData.getMsg());
                            log.info("添加芯烨打印机返回信息"+resultData.getData().getSuccess().toString());
                            //resp.data:返回1个 json 对象，包含成功和失败的信息，详看https://www.xpyun.net/open/index.html示例
                            List<String> success1 = resultData.getData().getSuccess();
                            if (!success1.isEmpty()) {
                                //新增打印机
                                List<Long> printerSettingIds = new ArrayList<>();
                                //如果打印机设置内容不是空
                                if (!printerTable.getPrinterSettingList().isEmpty()) {
                                    //遍历打印机设置
                                    for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                                        //新增打印机设置
//                                    printerSetting.setProjectOwnerShip(projectOwnerShip);
                                        printerSettingService.insertPrinterSetting(printerSetting);
                                        //然后把 id 添加到集合里
                                        printerSettingIds.add(printerSetting.getPrinterSettingId());
                                    }
                                    //将打印机设置的 id 集合存到打印机表里
                                    printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
                                }else{
                                    printerTable.setPrinterSettingIds(null);
                                }
                                //新增打印机
                                printerTable.setBusinessId(businessId);
                                printerTableMapper.updatePrinterTable(printerTable);
                                return true;
                            }
                        } else {
                            throw exception(SYSTEM_PRINTER_DELETE_FAIL);
                        }
                    }
                    case "飞鹅": {
                        //通过POST请求，发送打印信息到服务器
                        RequestConfig requestConfig = RequestConfig.custom()
                                .setSocketTimeout(30000)//读取超时
                                .setConnectTimeout(30000)//连接超时
                                .build();
                        CloseableHttpClient httpClient = HttpClients.custom()
                                .setDefaultRequestConfig(requestConfig)
                                .build();
                        PrinterBrandTable printerBrandTable = new PrinterBrandTable();
                        printerBrandTable.setPrinterBrandName(printerTable1.getPrinterBrand());
                        PrinterBrandTable printerBrandTable1 = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);
                        PrinterInterface printerInterface = new PrinterInterface();
                        printerInterface.setPrinterInterfaceAbility(4L);
                        printerInterface.setPrinterBrandId(printerBrandTable1.getPrinterBrandId());
                        PrinterInterface printerInterface1 = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);
                        HttpPost post = new HttpPost(printerBrandTable1.getPrinterBrandInterface());
                        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                        nvps.add(new BasicNameValuePair("user", printerBrandTable1.getPrinterBrandUser()));
                        String STIME = String.valueOf(System.currentTimeMillis() / 1000);
                        nvps.add(new BasicNameValuePair("stime", STIME));
                        nvps.add(new BasicNameValuePair("sig", signature(printerBrandTable1.getPrinterBrandUser(), printerBrandTable1.getPrinterBrandUserkey(), STIME)));
                        nvps.add(new BasicNameValuePair("apiname", printerInterface1.getPrinterInterfaceValue()));
                        nvps.add(new BasicNameValuePair("snlist", printerTable1.getPrinterSerialNumber()));
                        CloseableHttpResponse response = null;
                        String result = null;
                        try {
                            post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
                            response = httpClient.execute(post);
                            int statecode = response.getStatusLine().getStatusCode();
                            if (statecode == 200) {
                                HttpEntity httpentity = response.getEntity();
                                if (httpentity != null) {
                                    result = EntityUtils.toString(httpentity);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (response != null) {
                                    response.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                post.abort();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                httpClient.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }



                        //将打印机品牌赋值
                        printerBrandTable.setPrinterBrandName(printerBrand);
                        //查询出该品牌下的打印机品牌信息
                        PrinterBrandTable printerBrandTables = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);
                        //开始查询此打印机功能方式
                        //设置打印机品牌 id
                        printerInterface.setPrinterBrandId(printerBrandTables.getPrinterBrandId());
                        //设置打印机功能编号
                        printerInterface.setPrinterInterfaceAbility(1L);
                        //拿到功能内容
                        PrinterInterface printerInterfaces = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);


                        PrinterTable printerTable2 = new PrinterTable();
                        printerTable2.setPrinterBrand(printerBrand);
                        printerTable2.setPrinterSerialNumber(printerTable.getPrinterSerialNumber());
                        List<PrinterTable> printerTables = printerTableMapper.selectPrinterTableList(printerTable2);
                        if (printerTables.isEmpty()) {
                            RequestConfig requestConfigData = RequestConfig.custom()
                                    .setSocketTimeout(30000)//读取超时
                                    .setConnectTimeout(30000)//连接超时
                                    .build();
                            CloseableHttpClient httpClientData = HttpClients.custom()
                                    .setDefaultRequestConfig(requestConfigData)
                                    .build();
                            HttpPost postData = new HttpPost(printerBrandTables.getPrinterBrandInterface());
                            String savePrinterContent = printerTable.getPrinterSerialNumber() + "#" + printerTable.getPrinterSerialKey() + "#" + printerTable.getPrinterName();
                            List<NameValuePair> nvpsTwo = new ArrayList<NameValuePair>();
                            nvpsTwo.add(new BasicNameValuePair("user", printerBrandTables.getPrinterBrandUser()));
                            String STIMES = String.valueOf(System.currentTimeMillis() / 1000);
                            nvpsTwo.add(new BasicNameValuePair("stime", STIMES));
                            nvpsTwo.add(new BasicNameValuePair("sig", signature(printerBrandTables.getPrinterBrandUser(), printerBrandTables.getPrinterBrandUserkey(), STIME)));
                            nvpsTwo.add(new BasicNameValuePair("apiname", printerInterfaces.getPrinterInterfaceValue()));
                            nvpsTwo.add(new BasicNameValuePair("printerContent", savePrinterContent));
                            CloseableHttpResponse responseTwo = null;
                            String resultData;
                            try {
                                postData.setEntity(new UrlEncodedFormEntity(nvpsTwo, "utf-8"));
                                responseTwo = httpClientData.execute(postData);
                                int statecode = responseTwo.getStatusLine().getStatusCode();
                                log.info(">>> 添加飞鹅打印机 接口调用状态返回" + statecode);
                                if (statecode == 200) {
                                    HttpEntity httpentity = responseTwo.getEntity();
                                    resultData = EntityUtils.toString(httpentity);
                                    JSONObject jsonObject = JSONObject.parseObject(resultData);
                                    //log.info(">>> 添加飞鹅打印机 接口返回信息 {}", jsonObject);
                                    if (!"ok".equals(jsonObject.get("msg").toString())) {

                                        throw new ServiceException(jsonObject.get("msg").toString());
                                    }
                                    JSONObject data = (JSONObject) jsonObject.get("data");
                                    JSONArray no = JSONArray.parseArray(JSON.toJSONString(data.get("no")));
                                    if (no.size() > 0) {
                                        Object o = no.get(0).toString();

                                        throw exception(SYSTEM_PRINTER_GESHI_ERROR);
                                    }
                                } else {
                                    throw exception(SYSTEM_PRINTER_REGISTER_ERROR);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw exception(SYSTEM_PRINTER_INSERT_ERROR);
                            } finally {
                                try {
                                    if (responseTwo != null) {
                                        responseTwo.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    postData.abort();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    httpClientData.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            //新增打印机
                            List<Long> printerSettingIds = new ArrayList<>();
                            //如果打印机设置内容不是空
                            if (!printerTable.getPrinterSettingList().isEmpty()) {
                                //遍历打印机设置
                                for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                                    //新增打印机设置
//                                printerSetting.setProjectOwnerShip(projectOwnerShip);
                                    printerSettingMapper.insert(printerSetting);
                                    //然后把 id 添加到集合里
                                    printerSettingIds.add(printerSetting.getPrinterSettingId());
                                }
                                //将打印机设置的 id 集合存到打印机表里
                                printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
                            }else {
                                printerTable.setPrinterSettingIds(null);
                            }

                            printerTable.setBusinessId(businessId);
                            printerTableMapper.updatePrinterTable(printerTable);
                            return true;
                        }
                        return true;

                    }
                }
            } else {
                //初始化打印机设置id 集合
                List<Long> printerSettingIds = new ArrayList<>();
                //如果打印机的设置列表不是空
                if (!printerTable.getPrinterSettingList().isEmpty()) {
                    //遍历打印机设置列表
                    for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                        //如果打印机设置的 id 不是空 说明是修改打印机设置
                        if (printerSetting.getPrinterSettingId() != null) {
                            //那就走修改方法
                            printerSettingService.updatePrinterSetting(printerSetting);
                            //将打印机的 id 拿出来放到打印机 id集合里去
                            printerSettingIds.add(printerSetting.getPrinterSettingId());
                        } else {
                            //如果修改的打印机设置没有 id 说名是新增打印机设置
//                            printerSetting.setProjectOwnerShip(projectOwnerShip);
                            printerSettingService.insertPrinterSetting(printerSetting);
                            //然后还是把 id 添加到集合里去
                            printerSettingIds.add(printerSetting.getPrinterSettingId());
                        }
                    }
                    //然后把集合变成字符串存到打印机 ids 的字段里去
                    printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
                }else{
                    printerTable.setPrinterSettingIds(null);
                }
                printerTableMapper.updatePrinterTable(printerTable);
                return true;
            }

        }
        return true;
    }
    private void updatePrinterSettings(PrinterTable printerTable) {
        List<Long> printerSettingIds = new ArrayList<>();

        if (!printerTable.getPrinterSettingList().isEmpty()) {
            for (PrinterSetting setting : printerTable.getPrinterSettingList()) {
                if (setting.getPrinterSettingId() != null) {
                    printerSettingService.updatePrinterSetting(setting);
                } else {
                    printerSettingService.insertPrinterSetting(setting);
                }
                printerSettingIds.add(setting.getPrinterSettingId());
            }
            printerTable.setPrinterSettingIds(convertListToString(printerSettingIds));
        } else {
            printerTable.setPrinterSettingIds(null);
        }
    }

    /**
     * 查询门店打印机
     *
     * @param printerTableId 门店打印机主键
     * @return 门店打印机
     */
    @Override
    public PrinterTable selectPrinterTableByPrinterTableId(Long printerTableId) {
        List<PrinterSetting> printerSettingList = new ArrayList<>();
        PrinterTable printerTable = printerTableMapper.selectPrinterTableByPrinterTableId(printerTableId);
        if(printerTable!=null){
            if (ObjectUtil.isNotEmpty(printerTable.getPrinterSettingIds())) {
                List<Long> printerSettingIds = convertStringToList(printerTable.getPrinterSettingIds());
                printerSettingList = printerSettingService.selectPrinterSettingListByIds(printerSettingIds);
            }
        }

        printerTable.setPrinterSettingList(printerSettingList);
        return printerTable;
    }

    @Override
    public PageResult<PrinterTable> selectByPageList(Long storeId) {
        Long businessId = BusinessContextHolder.getBusinessId();
        PrinterTable printerTable = new PrinterTable();
        printerTable.setBusinessId(businessId);
        printerTable.setStoreId(storeId);
        Page<PrinterTable> list = printerTableService.selectPrinterTableList(printerTable);
        PageResult<PrinterTable> pageResult = new PageResult<>();
        pageResult.setTotal(list.getTotal());
        List<PrinterTable> records = list.getRecords();
        LambdaQueryWrapper<SystemStoreInfoDO> systemStoreInfoDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        systemStoreInfoDOLambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId,storeId);
//        systemStoreInfoDOLambdaQueryWrapper.eq(SystemStoreInfoDO::getBusinessId,businessId);
        SystemStoreInfoDO storeInfoDO = systemStoreInfoMapper.selectOne(systemStoreInfoDOLambdaQueryWrapper);
        for (PrinterTable record : records) {
            int n1 = 0;
            int n2 = 0;
            int n3 = 0;
            int n4 = 0;
            Set<Integer> templateIdSet = new HashSet<>();
            if (!org.springframework.util.StringUtils.isEmpty(record.getPrinterSettingIds())) {
                List<PrinterSetting> printerSettingList = new ArrayList<>();
                String printerSettingIds = record.getPrinterSettingIds();
                String[] parts = printerSettingIds.split(",");
                List<Long> printerSettingId = convertStringToList(printerSettingIds);
                printerSettingList = printerSettingService.selectPrinterSettingListByIds(printerSettingId);
                record.setPrinterSettingList(printerSettingList);
                for (String part : parts) {
                    long l = Long.parseLong(part);
                    LambdaQueryWrapper<PrinterSetting> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(PrinterSetting::getPrinterSettingId, l);
                    PrinterSetting printerSetting = printerSettingMapper.selectOne(lambdaQueryWrapper);



                    templateIdSet.add(printerSetting.getDocumentType());
                    if (printerSetting != null) {
                        if (printerSetting.getDocumentType().equals(1)) {
                            n1 = printerSetting.getPrintCopies();
                        } else if (printerSetting.getDocumentType().equals(2)) {
                            n2 = printerSetting.getPrintCopies();
                        } else if (printerSetting.getDocumentType().equals(3)) {
                            n3 = printerSetting.getPrintCopies();
                        } else if (printerSetting.getDocumentType().equals(4)) {
                            n4 = printerSetting.getPrintCopies();
                        }

                    }


                }
            }
            record.setPrinterSettingString("商家联x" + n1 + "," + "后厨联x" + n2 + "," + "顾客联x" + n3 + "," + "配送联x" + n4);
            /**
             * 返回小票模板
             */
            if (StringUtils.isNotBlank(storeInfoDO.getPrinterTemplate())) {
                PrintConfigVO printConfigVO = JSON.parseObject(storeInfoDO.getPrinterTemplate(), PrintConfigVO.class);
                int printerType = 1;
                if (record.getPrinterBrand().contains("芯烨")) {
                    printerType = 2;
                }
                boolean showCampusDeliveryAmount = Objects.equals(storeInfoDO.getCampusDeliveryStatus(), 0);
                List<PrintCommodityTempVO> printerTemplateList = MultiReceiptTemplate.getTemplateDC(templateIdSet, printerType, printConfigVO, storeInfoDO.getOrderType(), showCampusDeliveryAmount);

                int finalN1 = n1;
                int finalN2 = n2;
                int finalN3 = n3;
                int finalN4 = n4;
                printerTemplateList.forEach(printCommodityTempVO -> {
                    PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(printCommodityTempVO.getTemplateType());
                    switch (printerTomplateTypeEnum) {
                        case STORE:
                            printCommodityTempVO.setPrintNum(finalN1);
                            break;
                        case MEMBER:
                            printCommodityTempVO.setPrintNum(finalN3);
                            break;
                        case KITCHEN:
                            printCommodityTempVO.setPrintNum(finalN2);
                            break;
                        case DELIVERY:
                            printCommodityTempVO.setPrintNum(finalN4);
                            break;
                    }
                    record.setPrinterTemplateList(printerTemplateList);
                });
            }
        }
        pageResult.setList(records);
        return pageResult;
    }
    @Override
    public PageResult<PrinterTable> selectByPageListApp(Long storeId) {
        PrinterTable printerTable = new PrinterTable();
        printerTable.setStoreId(storeId);
        Page<PrinterTable> list = printerTableService.selectPrinterTableList(printerTable);
        PageResult<PrinterTable> pageResult = new PageResult<>();
        pageResult.setTotal(list.getTotal());
        List<PrinterTable> records = list.getRecords();
        SystemStoreInfoDO storeInfoDO = systemStoreInfoMapper.selectById(storeId);
        Set<Integer> templateIdSet = new HashSet<>();
        for (PrinterTable record : records) {
            int n1 = 0;
            int n2 = 0;
            int n3 = 0;
            int n4 = 0;

            if (!org.springframework.util.StringUtils.isEmpty(record.getPrinterSettingIds())) {
                List<PrinterSetting> printerSettingList = new ArrayList<>();
                String printerSettingIds = record.getPrinterSettingIds();
                String[] parts = printerSettingIds.split(",");
                List<Long> printerSettingId = convertStringToList(printerSettingIds);
                printerSettingList = printerSettingService.selectPrinterSettingListByIds(printerSettingId);
                record.setPrinterSettingList(printerSettingList);
                for (String part : parts) {
                    long l = Long.parseLong(part);
                    LambdaQueryWrapper<PrinterSetting> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(PrinterSetting::getPrinterSettingId, l);
                    PrinterSetting printerSetting = printerSettingMapper.selectOne(lambdaQueryWrapper);



                    templateIdSet.add(printerSetting.getDocumentType());
                    if (printerSetting != null) {
                        if (printerSetting.getDocumentType().equals(1)) {
                            n1 = printerSetting.getPrintCopies();
                        } else if (printerSetting.getDocumentType().equals(2)) {
                            n2 = printerSetting.getPrintCopies();
                        } else if (printerSetting.getDocumentType().equals(3)) {
                            n3 = printerSetting.getPrintCopies();
                        } else if (printerSetting.getDocumentType().equals(4)) {
                            n4 = printerSetting.getPrintCopies();
                        }

                    }


                }
            }
            record.setPrinterSettingString("商家联x" + n1 + "," + "后厨联x" + n2 + "," + "顾客联x" + n3 + "," + "配送联x" + n4);
            /**
             * 返回小票模板
             */
            if (StringUtils.isNotBlank(storeInfoDO.getPrinterTemplate())) {
                PrintConfigVO printConfigVO = JSON.parseObject(storeInfoDO.getPrinterTemplate(), PrintConfigVO.class);
                int printerType = 1;
                if (record.getPrinterBrand().contains("芯烨")) {
                    printerType = 2;
                }
                List<PrintCommodityTempVO> printerTemplateList = MultiReceiptTemplate.getTemplateApp(templateIdSet, printerType, printConfigVO);

                int finalN1 = n1;
                int finalN2 = n2;
                int finalN3 = n3;
                int finalN4 = n4;
                printerTemplateList.forEach(printCommodityTempVO -> {
                    PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(printCommodityTempVO.getTemplateType());
                    switch (printerTomplateTypeEnum) {
                        case STORE:
                            printCommodityTempVO.setPrintNum(finalN1);
                            break;
                        case MEMBER:
                            printCommodityTempVO.setPrintNum(finalN3);
                            break;
                        case KITCHEN:
                            printCommodityTempVO.setPrintNum(finalN2);
                            break;
                        case DELIVERY:
                            printCommodityTempVO.setPrintNum(finalN4);
                            break;
                    }
                    record.setPrinterTemplateList(printerTemplateList);
                });
            }
        }
        pageResult.setList(records);
        return pageResult;
    }

    /**
     * 删除门店打印机信息
     *
     * @param printerTableId 门店打印机主键
     * @return 结果
     */
    @Override
    public boolean deletePrinterTableByPrinterTableId(Long printerTableId) {

        PrinterTable printerTable1 = printerTableMapper.selectPrinterTableByPrinterTableId(printerTableId);
        if(ObjectUtil.isEmpty(printerTable1)){
            return false;
        }
        if (printerTable1.getPrinterType() == 1) {
            printerTableMapper.deletePrinterTableByPrinterTableId(printerTableId);
            return true;
        }

        switch (printerTable1.getPrinterBrand()) {
            case "芯烨": {
                DelPrinterRequest request = new DelPrinterRequest();
                //*必填*：打印机编号集合，类型为字符串数组
                String[] snlist = new String[1];
                //*必填*：打印机编号
                snlist[0] = printerTable1.getPrinterSerialNumber();
                PrinterBrandTable printerBrandTable = new PrinterBrandTable();
                printerBrandTable.setPrinterBrandName(printerTable1.getPrinterBrand());
                PrinterBrandTable printerBrandTable1 = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);

                request.setUser(printerBrandTable1.getPrinterBrandUser());
                request.setTimestamp(System.currentTimeMillis() + "");
                //*必填*：对参数 user + UserKEY + timestamp 拼接后（+号表示连接符）进行SHA1加密得到签名，值为40位小写字符串，其中 UserKEY 为用户开发者密钥
                request.setSign(HashSignUtil.sign(request.getUser() + printerBrandTable1.getPrinterBrandUserkey() + request.getTimestamp()));
                request.setSnlist(snlist);
                PrinterInterface printerInterface = new PrinterInterface();
                printerInterface.setPrinterBrandId(printerBrandTable1.getPrinterBrandId());
                printerInterface.setPrinterInterfaceAbility(4L);
                PrinterInterface printerInterface1 = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);
                String url = printerBrandTable1.getPrinterBrandInterface() + printerInterface1.getPrinterInterfaceValue();
                String jsonRequest = JSON.toJSONString(request);
                String resp = HttpClientUtil.doPostJSON(url, jsonRequest);
                ObjectRestResponse<PrinterResult> result = JSON.parseObject(resp, new TypeReference<ObjectRestResponse<PrinterResult>>() {
                });
                if (Objects.equals(result.getMsg(), "ok")) {
                    printerTableMapper.deletePrinterTableByPrinterTableId(printerTableId);
                    return true;
                } else {
                    throw exception(SYSTEM_PRINTER_DELETE_FAIL);
                }
            }
            case "飞鹅": {
                //通过POST请求，发送打印信息到服务器
                RequestConfig requestConfig = RequestConfig.custom()
                        .setSocketTimeout(30000)//读取超时
                        .setConnectTimeout(30000)//连接超时
                        .build();
                CloseableHttpClient httpClient = HttpClients.custom()
                        .setDefaultRequestConfig(requestConfig)
                        .build();
                PrinterBrandTable printerBrandTable = new PrinterBrandTable();
                printerBrandTable.setPrinterBrandName(printerTable1.getPrinterBrand());
                PrinterBrandTable printerBrandTable1 = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);
                PrinterInterface printerInterface = new PrinterInterface();
                printerInterface.setPrinterInterfaceAbility(4L);
                printerInterface.setPrinterBrandId(printerBrandTable1.getPrinterBrandId());
                PrinterInterface printerInterface1 = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);
                HttpPost post = new HttpPost(printerBrandTable1.getPrinterBrandInterface());
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("user", printerBrandTable1.getPrinterBrandUser()));
                String STIME = String.valueOf(System.currentTimeMillis() / 1000);
                nvps.add(new BasicNameValuePair("stime", STIME));
                nvps.add(new BasicNameValuePair("sig", signature(printerBrandTable1.getPrinterBrandUser(), printerBrandTable1.getPrinterBrandUserkey(), STIME)));
                nvps.add(new BasicNameValuePair("apiname", printerInterface1.getPrinterInterfaceValue()));
                nvps.add(new BasicNameValuePair("snlist", printerTable1.getPrinterSerialNumber()));
                CloseableHttpResponse response = null;
                String result = null;
                try {
                    post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
                    response = httpClient.execute(post);
                    int statecode = response.getStatusLine().getStatusCode();
                    if (statecode == 200) {
                        HttpEntity httpentity = response.getEntity();
                        if (httpentity != null) {
                            result = EntityUtils.toString(httpentity);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (response != null) {
                            response.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        post.abort();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                printerTableMapper.deletePrinterTableByPrinterTableId(printerTableId);
                return true;

            }
        }
        return true;
    }


    @Override
    public boolean delPrinterQueue(PrinterTable printerTables) {

        PrinterTable printerTable = printerTableMapper.selectPrinterTableByPrinterTableId(printerTables.getPrinterTableId());

        switch (printerTable.getPrinterBrand()) {
            case "芯烨": {
                PrinterRequest request = new PrinterRequest();
                request.setSn(printerTable.getPrinterSerialNumber());
                //*必填*：当前UNIX时间戳
                request.setTimestamp(System.currentTimeMillis() + "");
                PrinterBrandTable printerBrandTable = new PrinterBrandTable();
                printerBrandTable.setPrinterBrandName(printerTable.getPrinterBrand());
                PrinterBrandTable printerBrandTable1 = printerBrandTableService.selectPrinterBrandTableList(printerBrandTable).get(0);
                //*必填*：对参数 user + UserKEY + timestamp 拼接后（+号表示连接符）进行SHA1加密得到签名，值为40位小写字符串，其中 UserKEY 为用户开发者密钥
                request.setSign(HashSignUtil.sign(request.getUser() + printerBrandTable1.getPrinterBrandUserkey() + request.getTimestamp()));
                request.setUser(printerBrandTable1.getPrinterBrandUser());
                PrinterInterface printerInterface = new PrinterInterface();
                printerInterface.setPrinterBrandId(printerBrandTable1.getPrinterBrandId());
                printerInterface.setPrinterInterfaceAbility(7L);
                PrinterInterface printerInterface1 = printerInterfaceService.selectPrinterInterfaceList(printerInterface).get(0);
                String url = printerBrandTable1.getPrinterBrandInterface() + printerInterface1.getPrinterInterfaceValue();
                String jsonRequest = JSON.toJSONString(request);
                String resp = HttpClientUtil.doPostJSON(url, jsonRequest);
                ObjectRestResponse<Boolean> result = JSON.parseObject(resp, new TypeReference<ObjectRestResponse<Boolean>>() {
                });
                return true;
            }
            case "飞蛾": {

            }
        }


        return true;
    }

    /**
     * 修改打印机打印内容
     *
     * @param printerTable
     */
    @Override
    public void updatePrinter(PrinterTable printerTable) {
        PrinterTable oldPrinterTable = printerTableMapper.selectPrinterTableByPrinterTableId(printerTable.getPrinterTableId());
        List<Long> printerSettingIds = convertStringToList(printerTable.getPrinterSettingIds());
        if (CollectionUtils.isNotEmpty(printerSettingIds)) {
            printerSettingService.delPrinterSetting(printerSettingIds);
        }
        BeanUtils.toBean(printerTable, oldPrinterTable.getClass());
        if (CollectionUtils.isNotEmpty(printerTable.getPrinterSettingList())) {
            List<Long> setIds = new ArrayList<>();
            for (PrinterSetting printerSetting : printerTable.getPrinterSettingList()) {
                printerSettingService.insertPrinterSetting(printerSetting);
                setIds.add(printerSetting.getPrinterSettingId());
            }
            oldPrinterTable.setPrinterSettingIds(convertListToString(setIds));
        }
        printerTableMapper.updatePrinterTable(oldPrinterTable);
    }

    @Override
    public void updatePrinterTemplate(PrinterTomplateReqVO printerTomplateReqVO) {
        SystemStoreInfoDO systemStoreInfo = systemStoreInfoMapper.selectById(printerTomplateReqVO.getStoreId());
        PrintConfigVO printConfigVO = new PrintConfigVO();
        if (Objects.isNull(systemStoreInfo)) {
            throw exception(STORE_NOT_EXISTS);
        }
        if (StringUtils.isNotEmpty(systemStoreInfo.getPrinterTemplate())) {
            printConfigVO = JSON.parseObject(systemStoreInfo.getPrinterTemplate(), PrintConfigVO.class);
        }
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(printerTomplateReqVO.getPrinterType());
        switch (printerTomplateTypeEnum) {
            case STORE:
                PrintInfoStoreVO printInfoStoreVO = new PrintInfoStoreVO();
                BeanUtils.copyProperties(printerTomplateReqVO, printInfoStoreVO);
                printConfigVO.setStore(printInfoStoreVO);
                break;
            case MEMBER:

                PrintInfoMemberVO printInfoMemberVO = new PrintInfoMemberVO();
                BeanUtils.copyProperties(printerTomplateReqVO, printInfoMemberVO);
                printConfigVO.setMember(printInfoMemberVO);
                break;
            case KITCHEN:

                PrintInfoKitchenVO printInfoKitchenVO = new PrintInfoKitchenVO();
                BeanUtils.copyProperties(printerTomplateReqVO, printInfoKitchenVO);
                printConfigVO.setKitchen(printInfoKitchenVO);
                break;
            case DELIVERY:

                PrintInfoDeliveryVO printInfoDeliveryVO = new PrintInfoDeliveryVO();
                BeanUtils.copyProperties(printerTomplateReqVO, printInfoDeliveryVO);
                printConfigVO.setDelivery(printInfoDeliveryVO);
                break;
        }
        systemStoreInfo.setPrinterTemplate(JSON.toJSONString(printConfigVO));
        systemStoreInfoMapper.updateById(systemStoreInfo);
    }

    /**
     * 获取模板
     */
    @Override
    public PrintConfigVO getPrinterTemplate(Long storeId) {
        SystemStoreInfoDO systemStoreInfo = systemStoreInfoMapper.selectById(storeId);
        PrintConfigVO printConfigVO = new PrintConfigVO();
        if (systemStoreInfo != null && systemStoreInfo.getPrinterTemplate() != null) {
            printConfigVO = JSON.parseObject(systemStoreInfo.getPrinterTemplate(), PrintConfigVO.class);
        }
        return printConfigVO;
    }

    //将 List <Long > 转成 string
    private static String convertListToString(List<Long> longList) {
        // 使用逗号连接 List<Long> 中的元素
        return longList.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    //将 String 里面 long，long 转成 list<long>
    private static List<Long> convertStringToList(String longListAsString) {
        List<Long> collect = Arrays.stream(longListAsString.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        collect.removeIf(Objects::isNull);
        // 使用逗号拆分字符串，并将每个部分转换为 Long 类型
        return collect;
    }

    private static String convertListToStringS(List<String> stringList) {
        // 使用逗号连接 List<String> 中的元素
        return String.join(",", stringList);
    }

    private static List<String> convertStringToListS(String stringListAsString) {
        // 使用逗号拆分字符串
        return Arrays.asList(stringListAsString.split(","));
    }

    //生成签名字符串
    private static String signature(String USER, String UKEY, String STIME) {
        String s = DigestUtils.sha1Hex(USER + UKEY + STIME);
        return s;
    }
}
