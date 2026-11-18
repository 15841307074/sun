package com.htyoudao.youdao.module.system.controller.admin.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSaveReqVO;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.util.store.StoreSaveRequestAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoreSaveRequestAdviceTest {

    private final ObjectMapper json = new ObjectMapper();
    private final SystemStoreInfoService service = mock(SystemStoreInfoService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        SystemStoreInfoController controller = new SystemStoreInfoController();
        ReflectionTestUtils.setField(controller, "systemStoreInfoService", service);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new StoreSaveRequestAdvice()).build();
    }

    @Test
    void filtersEmojiSequencesBeforeCreateValidationAndKeepsBusinessText() throws Exception {
        ObjectNode request = validRequest();
        request.put("storeName", "𠮷店👨‍👩‍👧‍👦®1️⃣");
        request.put("storeAddress", "一楼🇨🇳A区");
        request.put("storePhone", "138👍🏽00138000");
        request.put("storeAnnouncement", " ❤️ ");
        request.put("promptText", "");
        request.put("meituanId", "外部🧪编号");

        mvc.perform(post("/system/store/create").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<StoreSaveReqVO> captured = ArgumentCaptor.forClass(StoreSaveReqVO.class);
        verify(service).createStore(captured.capture());
        StoreSaveReqVO store = captured.getValue();
        assertEquals("𠮷店®", store.getStoreName());
        assertEquals("一楼A区", store.getStoreAddress());
        assertEquals("13800138000", store.getStorePhone());
        assertNull(store.getStoreAnnouncement());
        assertEquals("", store.getPromptText());
        assertEquals("外部🧪编号", store.getMeituanId());
    }

    @Test
    void returnsRequiredFieldMessagesAfterFilteringUpdate() throws Exception {
        ObjectNode request = validRequest();
        request.put("storeId", 7);
        request.put("storeName", " 🫩 ");
        request.put("storeLeader", "👨‍👩‍👧‍👦");
        request.put("storeLeaderPhone", " 👍🏽 ");
        request.put("storeAddress", "🇨🇳");

        var result = mvc.perform(put("/system/store/update").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest()).andReturn();

        var error = assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException());
        assertRequiredMessage(error, "storeName", "门店名称不能为空");
        assertRequiredMessage(error, "storeLeader", "门店负责人姓名不能为空");
        assertRequiredMessage(error, "storeLeaderPhone", "门店负责人电话不能为空");
        assertRequiredMessage(error, "storeAddress", "门店地址不能为空");
        verifyNoInteractions(service);
    }

    private ObjectNode validRequest() {
        ObjectNode request = json.createObjectNode();
        request.put("storeName", "门店");
        request.put("storeLeader", "张三");
        request.put("storeLeaderPhone", "13800138000");
        request.put("storeAddress", "一楼");
        request.put("userId", 1);
        request.put("orgId", 2);
        request.put("storePayType", "0");
        return request;
    }

    private static void assertRequiredMessage(MethodArgumentNotValidException error, String field, String message) {
        var fieldError = error.getBindingResult().getFieldError(field);
        assertNotNull(fieldError);
        assertEquals(message, fieldError.getDefaultMessage());
    }
}
