package com.example.demo.chatClient;

import com.example.demo.chatClient.domain.po.ChatDataPO;
import com.example.demo.chatClient.domain.po.ChildData;
import com.example.demo.chatClient.domain.record.ActorFilms;
import com.example.demo.chatClient.domain.record.MyChatClientWithParam;
import com.example.demo.chatClient.domain.record.MyChatClientWithSystem;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 本控制器主要是展示如何使用Spring AI的ChatClient，本示例中，使用了Spring AI的智普模型。
 * 如果想看细节日志，务必将日志级别调整至DEBUG
 */
@Slf4j
@RestController
class ChatClientController {

    private final ChatMemory chatMemory = new InMemoryChatMemory();

    // 注入带有系统文本的ChatClient
    private final ChatClient myChatClientWithSystem;

    // 注入系统文本带有参数的ChatClient
    private final ChatClient myChatClientWithParam;

    /**
     * 可以选择自动注入、也可以在方法内自定义，此客户端无系统文本
     */
    private final ChatClient chatClient;

    public ChatClientController(ChatClient.Builder chatClientBuilder, MyChatClientWithSystem myChatClient, MyChatClientWithParam myChatClientWithParam) {
        this.chatClient = chatClientBuilder.build();
        this.myChatClientWithSystem = myChatClient.client();
        this.myChatClientWithParam = myChatClientWithParam.client();

    }

    /**
     * 当前用户输入后，返回一个文本类型的回答
     * @param userInput
     * @return
     */
    @PostMapping("/ai")
    ChatDataPO generationByText(@RequestParam("userInput")  String userInput) {
        String content = this.myChatClientWithSystem.prompt()
                    .user(userInput)
                    .call()
                    .content();
        log.info("content: {}", content);
        ChatDataPO chatDataPO = ChatDataPO.builder().code("text").data(ChildData.builder().text(content).build()).build();;
        return chatDataPO;
    }

    /**
     * 当前用户输入后，返回一个实体类型的回答，为什么能封装呢？因为发送信息的时候，不仅仅发送了你写的文本，而且在后面加了一句话。如下：
     * Generate the filmography for a random actor.\r\nYour response should be in JSON format.\r\nDo not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.\r\nDo not include markdown code blocks in your response.\r\nRemove the ```json markdown from the output.\r\nHere is the JSON Schema instance your output must adhere to:\r\n```{\r\n \"$schema\" : \"https://json-schema.org/draft/2020-12/schema\",\r\n \"type\" : \"object\",\r\n \"properties\" : {\r\n \"actor\" : {\r\n \"type\" : \"string\"\r\n },\r\n \"movies\" : {\r\n \"type\" : \"array\",\r\n \"items\" : {\r\n \"type\" : \"string\"\r\n }\r\n }\r\n }\r\n}```\r\n
     * 所以在后面返回的时候，大模型返回的是：{\"actor\": \"Emily Blunt\", \"movies\": [\"Edge of Tomorrow\", \"A Quiet Place\", \"The Devil Wears Prada\", \"Sicario\", \"Mary Poppins Returns\"]}
     * @return
     */
    @GetMapping("/ai-Entity")
    ActorFilms generationByEntity() {
        ActorFilms actorFilms = chatClient.prompt()
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorFilms.class);
        return actorFilms;
    }

    /**
     *当前用户输入后，返回列表实体类型的回答，ParameterizedTypeReference是一个泛型，用于指定返回的类型。
     * @return
     */
    @GetMapping("/ai-EntityList")
    List<ActorFilms> generationByEntityList() {
        List<ActorFilms> actorFilms = chatClient.prompt()
                .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {
                });
        return actorFilms;
    }

    /**
     * 当前用户输入后，返回文本类型的回答，流式回答
     * @return
     */
    @GetMapping("/ai-stream")
    Flux<String> generationByStream() {
        Flux<String> output = chatClient.prompt()
                .user("Tell me a joke")
                .stream()
                .content();
        return output;
    }

    /**
     * 当前用户输入后，返回文本类型的回答，流式回答
     * @return
     */
    @GetMapping("/ai-streamWithParam")
    Flux<String> generationByStreamWithParam() {
        var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorFilms>>() {
        });

        Flux<String> flux = this.chatClient.prompt()
                .user(u -> u.text("""
                            Generate the filmography for a random actor.
                            {format}
                          """)
                        .param("format", converter.getFormat()))
                .stream()
                .content();

        String content = flux.collectList().block().stream().collect(Collectors.joining());

        List<ActorFilms> actorFilms = converter.convert(content);
        log.info("actorFilms: {}", actorFilms);
        return flux;
    }

    /**
     * 使用带有系统信息的client，当前用户输入后，返回一个map类型的回答，key为completion，value为回答
     * @param message  用户输入
     * @return
     */
    @GetMapping("/ai-withSystemClient")
    Map<String, String> generationByTextWithSystemClient(String message) {
        return Map.of("completion", myChatClientWithSystem.prompt().user(message).call().content());
    }

    /**
     * 使用系统带有参数信息的client，当前用户输入后，返回一个map类型的回答，key为completion，value为回答
     * @param message  用户输入
     * @return
     */
    @GetMapping("/ai-withParamClient")
    Map<String, String> generationByTextWithParamClient(String message, String user) {
        return Map.of("completion", myChatClientWithParam.prompt().system(sp ->sp.param("user",user)).user(message).call().content());
    }

    /**
     * 当前用户输入后，返回一个文本类型的回答
     * 在此示例中，将对矢量数据库中的所有文档执行相似性搜索。
     * @param userInput
     * @return
     */
    @PostMapping("/ai-chatMemory")
    ChatDataPO generationByChatMemory(HttpServletRequest request,@RequestParam("userInput")  String userInput) {
        String sessionId = request.getSession().getId();
        chatMemory.add(sessionId, new UserMessage(userInput));
        String content = this.myChatClientWithSystem.prompt()
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .user(userInput)
                .call()
                .content();
        chatMemory.add(sessionId, new AssistantMessage(content));
        log.info("content: {}", content);
        ChatDataPO chatDataPO = ChatDataPO.builder().code("text").data(ChildData.builder().text(content).build()).build();;
        return chatDataPO;
    }

}