package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.framework.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_SYSTEM_AGAIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryAdmissionBackpressureTest {
    @Test void rejectedBacklogDoesNotConsumeMemberDrawQuotaOrLeakConcurrency() {
        var redis=mock(StringRedisTemplate.class);when(redis.execute(any(RedisScript.class),anyList())).thenReturn(1L);
        var pressure=mock(LotteryGrantBackpressure.class);doThrow(exception(LOTTERY_SYSTEM_AGAIN)).doNothing().when(pressure).checkDrawAllowed();
        var admission=new LotteryAdmission();ReflectionTestUtils.setField(admission,"redis",redis);
        ReflectionTestUtils.setField(admission,"properties",new LotteryProperties());ReflectionTestUtils.setField(admission,"backpressure",pressure);
        assertThrows(ServiceException.class,()->admission.enter("draw",10,21,40L));
        try(var permit=admission.enter("draw",10,21,40L)){assertNotNull(permit);}
        verify(redis,times(1)).execute(any(RedisScript.class),argThat(keys->keys.size()==1&&keys.get(0).toString().endsWith("member:40")));
        assertEquals(0,((java.util.concurrent.atomic.AtomicInteger)ReflectionTestUtils.getField(admission,"draws")).get());
    }
}
