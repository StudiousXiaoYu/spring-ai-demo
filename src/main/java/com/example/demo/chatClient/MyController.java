package com.example.demo.chatClient;

import com.example.demo.chatClient.domain.record.ActorFilms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
class MyController {

    /**
     * 可以选择自动注入、也可以在方法内自定义
     */
    private final ChatClient chatClient;

    public MyController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 当前用户输入后，返回一个文本类型的回答
     * @param userInput
     * @return
     */
    @GetMapping("/ai")
    String generationByText(String userInput) {
        return this.chatClient.prompt()
            .user(userInput)
            .call()
            .content();
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
}