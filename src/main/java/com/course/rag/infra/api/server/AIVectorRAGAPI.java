package com.course.rag.infra.api.server;


import com.course.rag.infra.api.request.*;
import com.course.rag.service.RAGBasicIndexingService;
import com.course.rag.service.RAGVectorIndexingService;
import com.course.rag.service.RagBasicProcessorService;
import com.course.rag.service.RagVectorProcessorService;
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
@RequestMapping("/api/ai/rag/vector")
@Validated
public class AIVectorRAGAPI {

    @Autowired
    private RAGVectorIndexingService ragVectorIndexingService;

    @Autowired
    private RagVectorProcessorService ragVectorProcessorService;

    @PostMapping(value = "/indexing/document/filesystem", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> indexDocument(@RequestBody @Valid VectorIndexingRequestFromFileSystem request) {
        try {
            ragVectorIndexingService.indexDocumentFromFileSystem(request.path() , request.keywords()).subscribe(
                    processedDocuments -> System.out.println("Document processing complete! Added " + processedDocuments.size() + " documents."),
                    error -> System.err.println("Document processing failed: " + error.getMessage())
            );
            return ResponseEntity.ok("Documents indexed successfully from file system." );
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error indexing document: " + e.getMessage());
        }
    }


    @PostMapping(value = "/indexing/document/url", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> indexDocument(@RequestBody @Valid VectorIndexingRequestFromURl request) {
        try {
             ragVectorIndexingService.indexDocumentFromURL(request.url() ,  request.keywords()).subscribe(
                     processedDocuments -> System.out.println("Document processing complete! Added " + processedDocuments.size() + " documents."),
                     error -> System.err.println("Document processing failed: " + error.getMessage())
             );;
            return ResponseEntity.ok("Documents indexed successfully from URL. Chunk size: ");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error indexing document: " + e.getMessage());
        }
    }

    @PostMapping(value="/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> basicRAG(@RequestBody @Valid AIRequest request, @RequestParam(name ="top-k", required = false, defaultValue = "0") int topK) {
          return ragVectorProcessorService.generateRAGResponse(request.systemPrompt(), request.userPrompt());
    }

    @PostMapping(value="/ask/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> basicStreamRAG(@RequestBody @Valid AIRequest request, @RequestParam(name ="top-k", required = false, defaultValue = "0") int topK) {
        return ragVectorProcessorService.streamRagResponse(request.systemPrompt(), request.userPrompt());
    }
}
