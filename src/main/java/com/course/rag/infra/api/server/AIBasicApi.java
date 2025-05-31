package com.course.rag.infra.api.server;

import com.course.rag.infra.api.request.AIRequest;
import com.course.rag.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/ai")
@Validated
public class AIBasicApi {

    @Autowired
    private AIService aiService;

    @PostMapping(path = "/v1/basic", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> basicAI(@RequestBody @Valid AIRequest request) {

        return Mono.fromCallable(
                        () -> aiService.generateBasicResponse(
                                request.systemPrompt(),
                                request.userPrompt()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(path = "/v1/basic/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<String> basicStreamAI(@RequestBody @Valid AIRequest request) {
        var response = aiService.streamBasicResponse(request.systemPrompt(), request.userPrompt());

        return response;
    }

}
