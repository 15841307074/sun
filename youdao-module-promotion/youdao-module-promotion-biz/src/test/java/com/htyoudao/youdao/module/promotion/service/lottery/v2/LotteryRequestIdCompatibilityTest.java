package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.AppLotteryController;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryMobileService;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LotteryRequestIdCompatibilityTest {
    private LotteryMobileService mobile;
    private LotteryV2Service v2;
    private MockMvc mvc;
    @BeforeEach void setup() {
        var controller=new AppLotteryController();mobile=mock(LotteryMobileService.class);v2=mock(LotteryV2Service.class);
        ReflectionTestUtils.setField(controller,"identities",mock(LotteryIdentityService.class));
        ReflectionTestUtils.setField(controller,"lotteryMobileservice",mobile);ReflectionTestUtils.setField(controller,"lotteryV2Service",v2);
        when(mobile.lottery(any())).thenAnswer(call->{LotteryVO req=call.getArgument(0);var result=new LotteryUserLogVO();result.setRequestId(req.getRequestId());return CommonResult.success(result);});
        mvc=MockMvcBuilders.standaloneSetup(controller).setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
    }
    @Test void oldClientGetsUniqueRequestIdForEachDraw() throws Exception {
        for(String field:List.of("",",\"requestId\":null",",\"requestId\":\"\"",",\"requestId\":\"   \"")) {
            mvc.perform(post("/promotion/lottery-mobile/lottery").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"lotteryId\":30,\"memberId\":40,\"storeId\":50"+field+"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data.requestId").isNotEmpty());
        }
        var requests=ArgumentCaptor.forClass(LotteryVO.class);verify(mobile,times(4)).lottery(requests.capture());var ids=new HashSet<String>();
        for(var req:requests.getAllValues()) {assertTrue(req.getRequestId().matches("[A-Za-z0-9_-]{8,64}"));assertTrue(ids.add(req.getRequestId()));assertEquals(30L,req.getLotteryId());assertEquals(40L,req.getMemberId());assertEquals(50L,req.getStoreId());}
    }
    @Test void suppliedRequestIdRemainsUnchangedOnRetry() throws Exception {
        for(int i=0;i<2;i++)mvc.perform(post("/promotion/lottery-mobile/lottery").contentType(MediaType.APPLICATION_JSON)
                .content("{\"lotteryId\":30,\"memberId\":40,\"storeId\":50,\"requestId\":\"same-draw-1234\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.requestId").value("same-draw-1234"));
        verify(mobile,times(2)).lottery(argThat(req->"same-draw-1234".equals(req.getRequestId())));
    }
    @Test void invalidSuppliedIdStillReachesExistingBusinessValidation() throws Exception {
        mvc.perform(post("/promotion/lottery-mobile/lottery").contentType(MediaType.APPLICATION_JSON).content("{\"requestId\":\"bad!\"}"))
                .andExpect(status().isOk());verify(mobile).lottery(argThat(req->"bad!".equals(req.getRequestId())));
    }
    @Test void resultEndpointDoesNotGenerateRequestId() throws Exception {
        mvc.perform(post("/promotion/lottery-mobile/result").contentType(MediaType.APPLICATION_JSON).content("{\"lotteryId\":30,\"memberId\":40,\"storeId\":50}"))
                .andExpect(status().isOk());verify(v2).query(argThat(req->req.getRequestId()==null));verifyNoInteractions(mobile);
    }
}
