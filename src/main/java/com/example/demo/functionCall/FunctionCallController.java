package com.example.demo.functionCall;

import java.util.List;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.functionCall.service.MockWeatherService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class FunctionCallController {

    /**
     * 可以选择自动注入、也可以在方法内自定义，此客户端无系统文本
     */
    @Autowired
    public  ChatModel  chatModel;

    @GetMapping("/weather-call-name")
    public String weatherCallByName(String location) {
        UserMessage userMessage = new UserMessage(location);

        ChatResponse response = chatModel.call(new Prompt(List.of(userMessage),
                ZhiPuAiChatOptions.builder().withFunction("CurrentWeather").build())); // (1) Enable the function

        log.info("Response: {}", response);
        return response.toString();
    }
    
    @GetMapping("/weather-call-register")
    public String weatherCallByRegister(String location) {
        UserMessage userMessage = new UserMessage(location);
        var promptOptions = ZhiPuAiChatOptions.builder()
			.withModel(ZhiPuAiApi.ChatModel.GLM_3_Turbo.getValue())
            // .withUser("8824895038689981283LMKMK")
			.withFunctionCallbacks(List.of(FunctionCallbackWrapper.builder(new MockWeatherService())
				.withName("getCurrentWeather")
				.withDescription("Get the weather in location")
				.withResponseConverter((response) -> "" + response.temp() + response.unit())
				.build()))
			.build();
                            
        ChatResponse response = chatModel.call(new Prompt(userMessage, promptOptions));
        return response.toString();
    }
}
