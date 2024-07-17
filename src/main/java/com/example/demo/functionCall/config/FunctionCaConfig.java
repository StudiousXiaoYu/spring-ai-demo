package com.example.demo.functionCall.config;

import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.context.annotation.Bean;
import java.util.function.Function;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import com.example.demo.functionCall.service.MockWeatherService;
import com.fasterxml.jackson.annotation.JsonClassDescription;

// @Configuration
public class FunctionCaConfig {

    /**
     * 第一种方式
     * 直接定义函数，并且使用@Bean注解以及@Description注解，description注解用于描述函数的功能
     * @return
     */
    @Bean
    @Description("Get the weather in location") // function description
    public Function<MockWeatherService.Request, MockWeatherService.Response> weatherFunction1() {
        return new MockWeatherService();
    }

    /**
     * 第二种方式
     * 使用@Bean注解，但是不使用@Description注解,但是需要使用@JsonClassDescription注解配合使用
     */
    @Bean
	public Function<MockWeatherService.Request, MockWeatherService.Response> currentWeather3() { // (1) bean name as function name.
		return new MockWeatherService();
	}
 
    /**
     * 第三种方式
     * 使用FunctionCallbackWrapper类，使用编程的方式定义函数
     * @return
     */
    @Bean
	public FunctionCallback weatherFunctionInfo() {

    return FunctionCallbackWrapper.builder(new MockWeatherService())
        .withName("CurrentWeather") // (1) function name
        .withDescription("获取指定地点的天气情况") // (2) function description
        .build();
	}
}

// 和currentWeather3配对使用
@JsonClassDescription("Get the weather in location") // (2) function description
record Request(String location, MockWeatherService.Unit unit) {}