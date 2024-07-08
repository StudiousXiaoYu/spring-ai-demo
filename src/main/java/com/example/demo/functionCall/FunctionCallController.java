package com.example.demo.functionCall;

import java.util.List;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class FunctionCallController {

    /**
     * 可以选择自动注入、也可以在方法内自定义，此客户端无系统文本
     */
    private final ZhiPuAiChatModel  chatModel;

    public FunctionCallController(ZhiPuAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/weather-call")
    public String weatherCall(String location) {
        UserMessage userMessage = new UserMessage(location);

        ChatResponse response = chatModel.call(new Prompt(List.of(userMessage),
                ZhiPuAiChatOptions.builder().withFunction("CurrentWeather").build())); // (1) Enable the function

        log.info("Response: {}", response);
        return response.toString();
    }
    
}
