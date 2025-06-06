package com.course.rag.service;

import reactor.core.publisher.Flux;

public interface AIService {
    String generateBasicResponse(String systemPrompt, String userPrompt);

    Flux<String> streamBasicResponse(String systemPrompt, String userPrompt);
}
