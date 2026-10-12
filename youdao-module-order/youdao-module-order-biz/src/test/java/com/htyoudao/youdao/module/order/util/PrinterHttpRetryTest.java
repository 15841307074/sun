package com.htyoudao.youdao.module.order.util;

import com.htyoudao.youdao.module.order.util.feie.FeiESendUtil;
import com.htyoudao.youdao.module.order.util.xingye.XpYunSendUtil;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.SocketTimeoutException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PrinterHttpRetryTest {
    private final CloseableHttpClient client=mock(CloseableHttpClient.class);
    private final HttpPost request=new HttpPost("http://printer.test/print");
    private final ResponseHandler<String> handler=response->"ok";

    @Test void successfulSendIsNotRepeated() throws Exception {
        when(client.execute(request,handler)).thenReturn("ok");assertEquals("ok",PrinterHttpRetry.execute(client,request,handler,"test"));verify(client,times(1)).execute(request,handler);
    }
    @Test void responseTimeoutRetriesExactlyOnce() throws Exception {
        when(client.execute(request,handler)).thenThrow(new SocketTimeoutException("Read timed out")).thenReturn("ok");
        assertEquals("ok",PrinterHttpRetry.execute(client,request,handler,"test"));verify(client,times(2)).execute(request,handler);
    }
    @Test void connectionTimeoutRetriesExactlyOnce() throws Exception {
        when(client.execute(request,handler)).thenThrow(new ConnectTimeoutException("connect timeout")).thenReturn("ok");
        assertEquals("ok",PrinterHttpRetry.execute(client,request,handler,"test"));verify(client,times(2)).execute(request,handler);
    }
    @Test void secondTimeoutIsReturnedWithoutThirdAttempt() throws Exception {
        var last=new SocketTimeoutException("second timeout");when(client.execute(request,handler)).thenThrow(new ConnectTimeoutException("first timeout")).thenThrow(last);
        assertSame(last,assertThrows(SocketTimeoutException.class,()->PrinterHttpRetry.execute(client,request,handler,"test")));verify(client,times(2)).execute(request,handler);
    }
    @Test void otherConnectionErrorsAreNotRetried() throws Exception {
        var error=new IOException("connection refused");when(client.execute(request,handler)).thenThrow(error);
        assertSame(error,assertThrows(IOException.class,()->PrinterHttpRetry.execute(client,request,handler,"test")));verify(client,times(1)).execute(request,handler);
    }
    @Test void connectionPoolExhaustionIsNotRetried() throws Exception {
        when(client.execute(request,handler)).thenThrow(new ConnectionPoolTimeoutException("pool wait timeout"));
        assertThrows(ConnectionPoolTimeoutException.class,()->PrinterHttpRetry.execute(client,request,handler,"test"));verify(client,times(1)).execute(request,handler);
    }
    @Test void interruptedWorkerDoesNotStartAnotherRequest() throws Exception {
        when(client.execute(request,handler)).thenThrow(new SocketTimeoutException("timeout"));Thread.currentThread().interrupt();
        try {assertThrows(SocketTimeoutException.class,()->PrinterHttpRetry.execute(client,request,handler,"test"));verify(client,times(1)).execute(request,handler);}
        finally {Thread.interrupted();}
    }
    @Test void businessFailureResponseIsNotRetried() throws Exception {
        when(client.execute(request,handler)).thenReturn("business rejected");assertEquals("business rejected",PrinterHttpRetry.execute(client,request,handler,"test"));verify(client,times(1)).execute(request,handler);
    }
    @Test void feieReceiptUsesTimeoutRetryAndClosesClient() throws Exception {
        var builder=mock(HttpClientBuilder.class,RETURNS_SELF);when(builder.build()).thenReturn(client);
        when(client.execute(any(HttpUriRequest.class),org.mockito.ArgumentMatchers.<ResponseHandler<String>>any())).thenThrow(new SocketTimeoutException("read timeout")).thenReturn("receipt-ok");
        try(var factory=mockStatic(HttpClients.class)) {
            factory.when(HttpClients::custom).thenReturn(builder);assertEquals("receipt-ok",FeiESendUtil.print("test receipt","test-sn",2));
            verify(builder).disableAutomaticRetries();verify(client,times(2)).execute(any(HttpUriRequest.class),org.mockito.ArgumentMatchers.<ResponseHandler<String>>any());verify(client).close();
        }
    }
    @Test void xpyunReceiptUsesTimeoutRetryAndClosesClient() throws Exception {
        var builder=mock(HttpClientBuilder.class,RETURNS_SELF);when(builder.build()).thenReturn(client);
        when(client.execute(any(HttpUriRequest.class),org.mockito.ArgumentMatchers.<ResponseHandler<String>>any())).thenThrow(new ConnectTimeoutException("connect timeout")).thenReturn("{\"code\":0,\"data\":\"receipt-ok\"}");
        try(var factory=mockStatic(HttpClients.class)) {
            factory.when(HttpClients::custom).thenReturn(builder);assertEquals("receipt-ok",XpYunSendUtil.printComplexReceiptVoiceSupport("test receipt","test-sn",10D,2));
            verify(builder).disableAutomaticRetries();verify(client,times(2)).execute(any(HttpUriRequest.class),org.mockito.ArgumentMatchers.<ResponseHandler<String>>any());verify(client).close();
        }
    }
}
