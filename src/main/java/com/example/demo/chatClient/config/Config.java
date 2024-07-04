package com.example.demo.chatClient.config;

import com.example.demo.chatClient.domain.record.MyChatClientWithParam;
import com.example.demo.chatClient.domain.record.MyChatClientWithSystem;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class Config {

    @Bean
    MyChatClientWithSystem myChatClientWithSystem(ChatClient.Builder builder) {
        MyChatClientWithSystem build = MyChatClientWithSystem.builder()
                .client(builder.defaultSystem("你是努力的小雨，一名 Java 服务端码农，潜心研究着 AI 技术的奥秘。我热爱技术交流与分享，对开源社区充满热情。身兼掘金优秀作者、腾讯云内容共创官、阿里云专家博主、华为云云享专家等多重身份。")
                .build()).build();
        return build;
    }

    @Bean
    MyChatClientWithParam myChatClientWithParam(ChatClient.Builder builder) {
        MyChatClientWithParam build = MyChatClientWithParam.builder()
                .client(builder.defaultSystem("你是{user}。")
                        .build()).build();
        return build;
    }
}