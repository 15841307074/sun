package com.htyoudao.youdao.module.member.controller.admin.wecom;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.servlet.ServletUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.controller.admin.wecom.aes.AesException;
import com.htyoudao.youdao.module.member.controller.admin.wecom.aes.WXBizMsgCrypt;
import com.htyoudao.youdao.module.member.controller.admin.wecom.config.WecomConfig;
import com.htyoudao.youdao.module.member.service.wecom.WecomGroupService;
import com.htyoudao.youdao.module.member.service.wecom.WecomGroupVersionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Tag(name = "企业微信 - 回调事件")
@Slf4j
@RestController
@RequestMapping("/member/wecom")
public class WecomCallbackController {

    @Resource
    @Qualifier("wecomGroupDatabaseService")
    private WecomGroupService wecomGroupService;

    @Resource
    private WecomConfig wecomConfig;

    @Autowired
    private WecomGroupVersionUtil wecomGroupVersionUtil;


    @GetMapping("/callback")
    @PermitAll
    public String callback() {
        try {
            return verifyURL();
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * 处理企业微信回调通知
     */
    @PostMapping("/callback")
    @PermitAll
    public String callback(HttpServletRequest request, @RequestBody String requestBody) {
        log.info("{}", requestBody);
        BusinessContextHolder.setBusinessId(10L);

        String sReqMsgSig = request.getParameter("msg_signature");
        String sReqTimeStamp = request.getParameter("timestamp");
        String sReqNonce = request.getParameter("nonce");
        String sReqData = requestBody;
        try {
            WXBizMsgCrypt wxcpt = getWxBizMsgCrypt();
            String sMsg = wxcpt.DecryptMsg(sReqMsgSig, sReqTimeStamp, sReqNonce, sReqData);
            log.info(sMsg);

            // 解析XML格式的事件数据
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(sMsg);
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);

            Element root = document.getDocumentElement();

            // 提取事件类型
            String eventType = getElementText(root, "Event");
            log.info("事件类型: {}", eventType);

            // 处理客户群变更事件
            if ("change_external_chat".equals(eventType)) {
                handleChangeExternalChat(root);
            }

        } catch (Exception e) {
            log.error("处理回调事件失败", e);
        }
        return "success";
    }


    /**
     * resetChatData
     */
    @PostMapping("/resetChatData")
    @Operation(summary = "resetChatData")
    public CommonResult<String> resetChatData(@RequestParam String chatId) throws IOException {
        // 处理不可靠事件
        String version = wecomGroupService.handleUnreliableEvent(chatId);
        // 更新版本号
        wecomGroupVersionUtil.updateVersion(chatId, version);
        //
        return CommonResult.success(version);
    }



    /**
     * 处理客户群变更事件
     *
     * @param root XML根元素
     */
    private void handleChangeExternalChat(Element root) {
        try {
            // 提取事件数据
            String chatId = getElementText(root, "ChatId");
            String changeType = getElementText(root, "ChangeType");
            String updateDetail = getElementText(root, "UpdateDetail");
            String lastMemVer = getElementText(root, "LastMemVer");
            String curMemVer = getElementText(root, "CurMemVer");

            // 提取成员变更列表
            List<String> memChangeList = getMemChangeList(root);

            // 验证事件可靠性
            boolean isReliable = wecomGroupVersionUtil.validateEventReliability(chatId, lastMemVer);

            log.info(
                "=== {} 群变更事件: chatId={}, changeType={}, updateDetail={}。 ===",
                isReliable ? "可靠" : "不可靠", chatId, changeType, updateDetail
            );
            log.info("成员变更列表: {}", memChangeList);

            if (isReliable) {
                // 处理可靠事件
                wecomGroupService.handleReliableEvent(chatId, updateDetail, memChangeList);
                // 更新版本号
                wecomGroupVersionUtil.updateVersion(chatId, curMemVer);
            } else {
                // 处理不可靠事件
                String version = wecomGroupService.handleUnreliableEvent(chatId);

                // 更新版本号
                wecomGroupVersionUtil.updateVersion(chatId, version);
            }

        } catch (Exception e) {
            log.error("处理客户群变更事件失败", e);
        }
    }

    /**
     * 提取XML元素文本
     *
     * @param parent  父元素
     * @param tagName 标签名
     * @return 元素文本
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    /**
     * 提取成员变更列表
     *
     * @param root XML根元素
     * @return 成员变更列表
     */
    private List<String> getMemChangeList(Element root) {
        List<String> memChangeList = new ArrayList<>();
        NodeList itemList = root.getElementsByTagName("Item");
        if (itemList.getLength() > 0) {
            for (int i = 0; i < itemList.getLength(); i++) {
                String itemText = itemList.item(i).getTextContent();
                memChangeList.add(itemText);
            }
        }
        return memChangeList;
    }


    public String verifyURL() {
        HttpServletRequest request = ServletUtils.getRequest();
        String msgSignature = request.getParameter("msg_signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        return verifyURL(msgSignature, timestamp, nonce, echostr);
    }

    /**
     * 指令回调url验证 get请求
     *
     * @return
     */
    public String verifyURL(String sVerifyMsgSig, String sVerifyTimeStamp, String sVerifyNonce, String sVerifyEchoStr) {
        WXBizMsgCrypt wxcpt = getWxBizMsgCrypt();
        if (wxcpt == null) {
            return null;
        }
        String sEchoStr; //需要返回的明文
        try {
            sEchoStr = wxcpt.VerifyURL(sVerifyMsgSig, sVerifyTimeStamp,
                sVerifyNonce, sVerifyEchoStr);
        } catch (Exception e) {
            return "error";
        }
        return sEchoStr;
    }

    private WXBizMsgCrypt getWxBizMsgCrypt() {
        String sToken = wecomConfig.getToken();
        String sEncodingAESKey = wecomConfig.getEncodingAESKey();
        String sCorpID = wecomConfig.getCorpId();
        WXBizMsgCrypt wxcpt = null;
        try {
            wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);
        } catch (AesException E) {
            return null;
        }
        return wxcpt;
    }






}
