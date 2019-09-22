package com.immccc.aircontrol.plane.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.HandlerInterceptor;

import com.immccc.aircontrol.Application;
import com.immccc.aircontrol.core.interceptor.AccessLoggingInterceptor;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class PlaneControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testLogAccessIsCreated() throws Exception {
	MvcResult mvcResult = mockMvc.perform(get("/planes")).andReturn();

	Optional<HandlerInterceptor> interceptorFound = Arrays.asList(mvcResult.getInterceptors()).stream()
		.filter(interceptor -> interceptor instanceof AccessLoggingInterceptor).findAny();

	assertThat("Access log interceptor is present", interceptorFound.isPresent(), is(true));

    }
}
