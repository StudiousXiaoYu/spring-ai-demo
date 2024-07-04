package com.example.demo.chatClient.domain.record;

import lombok.Builder;
import org.springframework.ai.chat.client.ChatClient;

/**
 * 一个记录类（Record Class）的定义，名为 ActorFilms。用于封装相关字段
 * 记录类自动实现了 toString()、equals()、hashCode() 和 getter 方法，使得对象的字符串表示、相等性比较和哈希计算变得简单。
 * 你可以直接使用 actorFilms.toString()、actorFilms.equals(anotherActorFilms) 和 actorFilms.hashCode()。
 */
@Builder
public record MyChatClientWithParam(ChatClient client) {


}