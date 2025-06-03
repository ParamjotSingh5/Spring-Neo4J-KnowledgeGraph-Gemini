package com.course.rag.infra.api.server;


import com.course.rag.infra.api.request.AIRequest;
import com.course.rag.infra.api.request.BasicIndexingRequestFromFileSystem;
import com.course.rag.infra.api.request.BasicIndexingRequestFromURl;
import com.course.rag.service.RAGBasicIndexingService;
import com.course.rag.service.RagBasicProcessorService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/ai/rag/basic")
@Validated
public class AIBasicRAGAPI {

    @Autowired
    private RAGBasicIndexingService ragBasicIndexingService;

    @Autowired
    private RagBasicProcessorService ragBasicProcessorService;

    @PostMapping(value = "/indexing/document/filesystem", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> indexDocument(@RequestBody @Valid BasicIndexingRequestFromFileSystem request) {
        try {
            var documents = ragBasicIndexingService.indexDocumentFromFileSystem(request.path() , request.outputFilename(), request.appendIfFileExists(), request.keywords());
            return ResponseEntity.ok("Documents indexed successfully from file system. Chunk size: " + documents.size());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error indexing document: " + e.getMessage());
        }
    }


    @PostMapping(value = "/indexing/document/url", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> indexDocument(@RequestBody @Valid BasicIndexingRequestFromURl request) {
        try {
            var documents = ragBasicIndexingService.indexDocumentFromURL(request.url() , request.outputFilename(), request.appendIfFileExists(), request.keywords());
            return ResponseEntity.ok("Documents indexed successfully from URL. Chunk size: " + documents.size());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error indexing document: " + e.getMessage());
        }
    }

    @PostMapping(value="/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> basicRAG(@RequestBody @Valid AIRequest request, @RequestParam(value = "filename") @NotBlank String filenameForCustomContext) {
          return ragBasicProcessorService.generateRAGResponse(request.systemPrompt(), request.userPrompt(), filenameForCustomContext);
    }

    @PostMapping(value="/ask/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> basicStreamRAG(@RequestBody @Valid AIRequest request, @RequestParam(value = "filename") @NotBlank String filenameForCustomContext) {
        return ragBasicProcessorService.streamRagResponse(request.systemPrompt(), request.userPrompt(), filenameForCustomContext);
    }
}
