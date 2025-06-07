package com.course.rag.service;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;

@Service
public class RagVectorProcessorService {

    private static final String KEY_CUSTOM_CONTEXT = "customContext";
    private static final String KEY_QUESTION = "question";
    private static final int TOP_K = 4;
    private static final double SIMILARITY_THRESHOLD = 0.7;
    private final PromptTemplate promptTemplate;

    @Autowired
    private AIService aiService;
    @Autowired
    private Neo4jVectorStore vectorStore;


    private RagVectorProcessorService(){
        var ragBasicPromptTemplate = new ClassPathResource("prompts/rag-basic-template.st");
        this.promptTemplate = new PromptTemplate(ragBasicPromptTemplate);
    }

    private Mono<String> retrieveCustomContext(String userPrompt){
        return Mono.fromCallable(() -> {
            // This blocking operation will be executed on a dedicated thread
            var similarDocuments = vectorStore.similaritySearch(
                    SearchRequest.builder().query(userPrompt)
                            .topK(RagVectorProcessorService.TOP_K)
                            .similarityThreshold(SIMILARITY_THRESHOLD).build()
            );

            var customContext = new StringBuilder();

            if (similarDocuments != null) {
                similarDocuments.forEach(doc -> customContext.append(doc.getFormattedContent()).append("\n"));
            }

            return customContext.toString();
        }).subscribeOn(Schedulers.boundedElastic()); // Schedules the execution on an elastic thread pool suitable for blocking I/O
    }

    private String augmentUserPrompt(String originalUserPrompt, String customContext){
        var templateMap = new HashMap<String, Object>();

        templateMap.put(KEY_CUSTOM_CONTEXT, customContext);
        templateMap.put(KEY_QUESTION, originalUserPrompt);
        return promptTemplate.render(templateMap);
    }

    public Mono<String> generateRAGResponse(String systemPrompt, String userPrompt) {
        // 1. Start the reactive chain by calling the first async method.
        return retrieveCustomContext(userPrompt)
                // 2. Use 'flatMap' to chain the next asynchronous operation.
                //    'flatMap' waits for the result of retrieveCustomContext (the customContext String)
                //    and uses it to create the next Mono in the chain.
                .flatMap(customContext -> {
                    // This synchronous operation happens once customContext is available.
                    String augmentedUserPrompt = augmentUserPrompt(userPrompt, customContext);

                    // 3. Return the next Mono that wraps the blocking AI service call.
                    return Mono.fromCallable(() -> aiService.generateBasicResponse(systemPrompt, augmentedUserPrompt))
                            .subscribeOn(Schedulers.boundedElastic()); // Ensure the blocking call runs on a suitable thread.
                });
    }

    public Flux<String> streamRagResponse(String systemPrompt, String userPrompt) {
        return retrieveCustomContext(userPrompt)
                // 2. Use 'flatMapMany' to switch from a Mono to a Flux.
                //    It waits for the 'customContext' string from the Mono,
                //    then uses it to create a new Flux.
                .flatMapMany(customContext -> {
                    // This synchronous logic runs only AFTER the customContext is retrieved.
                    String augmentedUserPrompt = augmentUserPrompt(userPrompt, customContext);

                    // 3. Call the service that returns a Flux<String>.
                    //    This Flux is then returned by flatMapMany, completing the chain.
                    return aiService.streamBasicResponse(systemPrompt, augmentedUserPrompt);
                });
    }

}
