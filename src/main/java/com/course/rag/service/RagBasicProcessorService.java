package com.course.rag.service;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

@Service
public class RagBasicProcessorService {

    private static final String KEY_CUSTOM_CONTEXT = "customContext";
    private static final String KEY_QUESTION = "question";
    private final PromptTemplate promptTemplate;

    @Autowired
    private AIService aiService;

    public RagBasicProcessorService() {
        var ragBasicPromptTemplate = new ClassPathResource("prompts/rag-basic-template.st");
        promptTemplate = new PromptTemplate(ragBasicPromptTemplate);
    }

    private String retrieveCustomContext(String fromFilename) {
        try{
            return new String(Files.readAllBytes(Paths.get(fromFilename)));
        } catch (IOException e) {
            return "";
        }
    }

    private String augmentUserPrompt(String originalUserPrompt, String customContext) {
        var templateMap = new HashMap<String, Object>();

        templateMap.put(KEY_QUESTION, originalUserPrompt);
        templateMap.put(KEY_CUSTOM_CONTEXT, customContext);

        return promptTemplate.render(templateMap);
    }

    public String generateRAGResponse(String systemPrompt, String userPrompt, String filenameForCustomContext) {
        var customContext = retrieveCustomContext(filenameForCustomContext);
        var augmentUserPrompt = augmentUserPrompt(userPrompt, customContext);

        return aiService.generateBasicResponse(systemPrompt, augmentUserPrompt);
    }

    public Flux<String> streamRagResponse(String systemPrompt, String userPrompt, String filenameForCustomContext) {
        var customContext = retrieveCustomContext(filenameForCustomContext);
        var augmentUserPrompt = augmentUserPrompt(userPrompt, customContext);

        return aiService.streamBasicResponse(systemPrompt, augmentUserPrompt);
    }

}
