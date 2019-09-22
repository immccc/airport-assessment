package com.immccc.aircontrol.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.immccc.aircontrol.core.interceptor.AccessLoggingInterceptor;

@Configuration
public class CoreConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

	registry.addInterceptor(createAccessLogInterceptor());
    }

    private HandlerInterceptor createAccessLogInterceptor() {
	return new AccessLoggingInterceptor();
    }

}
