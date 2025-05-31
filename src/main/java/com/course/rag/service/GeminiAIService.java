package com.course.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class GeminiAIService implements AIService {

    @Autowired
    @Qualifier("GeminiChatClient")
    private ChatClient chatClient;

    @Override
    public String generateBasicResponse(String systemPrompt, String userPrompt) {
        Assert.hasText(userPrompt, "User prompt must not be empty");

        try{
          return this.chatClient.prompt()
                    .system(Optional.ofNullable(systemPrompt).orElse(""))
                    .user(userPrompt)
                    // .advisors(new SimpleLoggerAdvisor())
                    .call ().content();
        }
        catch (Exception e){
            System.out.println("Error in GeminiAIService: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Flux<String> streamBasicResponse(String systemPrompt, String userPrompt) {
        Assert.hasText(userPrompt, "User prompt must not be empty");

        return this.chatClient.prompt()
                .system(Optional.ofNullable(systemPrompt).orElse(""))
                .user(userPrompt)
                // .advisors(new SimpleLoggerAdvisor())
                .stream()
                .content();
    }
}
