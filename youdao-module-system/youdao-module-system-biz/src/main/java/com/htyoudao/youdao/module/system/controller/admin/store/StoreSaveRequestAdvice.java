package com.htyoudao.youdao.module.system.controller.admin.store;

import cn.hutool.core.util.StrUtil;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSaveReqVO;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice(assignableTypes = SystemStoreInfoController.class)
public class StoreSaveRequestAdvice extends RequestBodyAdviceAdapter {

    private static final Pattern GRAPHEME = Pattern.compile("\\X");

    // Emoji_Presentation ranges from the Unicode Emoji data, plus emoji variation and keycap sequences.
    // https://www.unicode.org/Public/17.0.0/ucd/emoji/emoji-data.txt
    private static final Pattern EMOJI = Pattern.compile("\\x{FE0F}|[#*0-9]\\x{20E3}|["
                    + "\\x{231A}-\\x{231B}\\x{23E9}-\\x{23EC}\\x{23F0}\\x{23F3}\\x{25FD}-\\x{25FE}\\x{2614}-\\x{2615}"
                    + "\\x{2648}-\\x{2653}\\x{267F}\\x{2693}\\x{26A1}\\x{26AA}-\\x{26AB}\\x{26BD}-\\x{26BE}"
                    + "\\x{26C4}-\\x{26C5}\\x{26CE}\\x{26D4}\\x{26EA}\\x{26F2}-\\x{26F3}\\x{26F5}\\x{26FA}\\x{26FD}"
                    + "\\x{2705}\\x{270A}-\\x{270B}\\x{2728}\\x{274C}\\x{274E}\\x{2753}-\\x{2755}\\x{2757}"
                    + "\\x{2795}-\\x{2797}\\x{27B0}\\x{27BF}\\x{2B1B}-\\x{2B1C}\\x{2B50}\\x{2B55}\\x{1F004}\\x{1F0CF}"
                    + "\\x{1F18E}\\x{1F191}-\\x{1F19A}\\x{1F1E6}-\\x{1F1FF}\\x{1F201}\\x{1F21A}\\x{1F22F}"
                    + "\\x{1F232}-\\x{1F236}\\x{1F238}-\\x{1F23A}\\x{1F250}-\\x{1F251}\\x{1F300}-\\x{1F320}"
                    + "\\x{1F32D}-\\x{1F335}\\x{1F337}-\\x{1F37C}\\x{1F37E}-\\x{1F393}\\x{1F3A0}-\\x{1F3CA}"
                    + "\\x{1F3CF}-\\x{1F3D3}\\x{1F3E0}-\\x{1F3F0}\\x{1F3F4}\\x{1F3F8}-\\x{1F43E}\\x{1F440}"
                    + "\\x{1F442}-\\x{1F4FC}\\x{1F4FF}-\\x{1F53D}\\x{1F54B}-\\x{1F54E}\\x{1F550}-\\x{1F567}\\x{1F57A}"
                    + "\\x{1F595}-\\x{1F596}\\x{1F5A4}\\x{1F5FB}-\\x{1F64F}\\x{1F680}-\\x{1F6C5}\\x{1F6CC}"
                    + "\\x{1F6D0}-\\x{1F6D2}\\x{1F6D5}-\\x{1F6D8}\\x{1F6DC}-\\x{1F6DF}\\x{1F6EB}-\\x{1F6EC}"
                    + "\\x{1F6F4}-\\x{1F6FC}\\x{1F7E0}-\\x{1F7EB}\\x{1F7F0}\\x{1F90C}-\\x{1F93A}\\x{1F93C}-\\x{1F945}"
                    + "\\x{1F947}-\\x{1F9FF}\\x{1FA70}-\\x{1FA7C}\\x{1FA80}-\\x{1FA8A}\\x{1FA8E}-\\x{1FAC6}\\x{1FAC8}"
                    + "\\x{1FACD}-\\x{1FADC}\\x{1FADF}-\\x{1FAEA}\\x{1FAEF}-\\x{1FAF8}"
                    + "]");

    @Override
    public boolean supports(MethodParameter parameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return targetType == StoreSaveReqVO.class;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        StoreSaveReqVO store = (StoreSaveReqVO) body;
        store.setStoreName(filterRequiredText(store.getStoreName()));
        store.setStoreLeader(filterRequiredText(store.getStoreLeader()));
        store.setStoreLeaderPhone(filterRequiredText(store.getStoreLeaderPhone()));
        store.setStoreAddress(filterRequiredText(store.getStoreAddress()));
        store.setStorePhone(filterEmoji(store.getStorePhone()));
        store.setDeliveryName(filterEmoji(store.getDeliveryName()));
        store.setDeliveryPhone(filterEmoji(store.getDeliveryPhone()));
        store.setStoreAnnouncement(filterEmoji(store.getStoreAnnouncement()));
        store.setPromptText(filterEmoji(store.getPromptText()));
        return store;
    }

    private static String filterRequiredText(String value) {
        String filtered = filterEmoji(value);
        return StrUtil.isBlank(filtered) ? null : filtered;
    }

    private static String filterEmoji(String value) {
        if (value == null || !EMOJI.matcher(value).find()) {
            return value;
        }
        // Remove the complete cluster, including skin tones, selectors and joiners.
        StringBuilder filtered = new StringBuilder(value.length());
        Matcher clusters = GRAPHEME.matcher(value);
        while (clusters.find()) {
            if (!EMOJI.matcher(clusters.group()).find()) {
                filtered.append(value, clusters.start(), clusters.end());
            }
        }
        return StrUtil.isBlank(filtered) ? null : filtered.toString();
    }
}
