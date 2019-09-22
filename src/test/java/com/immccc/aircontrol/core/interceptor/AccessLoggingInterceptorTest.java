package com.immccc.aircontrol.core.interceptor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AccessLoggingInterceptor.class, LoggerFactory.class })
public class AccessLoggingInterceptorTest {

    @Test
    public void testPostHandleLogsAccess() throws Exception {
	PowerMockito.mockStatic(LoggerFactory.class);
	Logger log = Mockito.mock(Logger.class);
	when(LoggerFactory.getLogger(Mockito.any(Class.class))).thenReturn(log);

	AccessLoggingInterceptor accessLoggingInterceptor = new AccessLoggingInterceptor();

	accessLoggingInterceptor.postHandle(new TestHttpServletRequest(), null, null, null);

	verify(log).info(anyString(), any(), any(), any());
    }
}
