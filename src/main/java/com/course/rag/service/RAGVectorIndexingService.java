package com.course.rag.service;

import com.course.rag.indexing.RAGDocumentFileWriter;
import com.course.rag.indexing.RAGTikaDocumentReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@Slf4j
public class RAGVectorIndexingService {

    @Autowired
    private TextSplitter textSplitter;

    @Autowired
    private RAGTikaDocumentReader tikaDocumentReader;

    @Autowired
    private Neo4jVectorStore vectorStore;

    private static final String CUSTOM_KEYWORDS_METADATA_KEY = "custom_keywords";


    public Mono<List<Document>> indexDocumentFromURL(String sourcePath, List<String> keywords) {
        // Read the document from the URL
        try{
            var resource =  new UrlResource(sourcePath);
            return processDocument(resource, keywords);
        }
        catch (Exception e){
            log.error("Error in indexDocumentFromURL: {}", e.getMessage());
            throw new RuntimeException("Error in indexDocumentFromURL: " + e.getMessage());
        }
    }

    public Mono<List<Document>> indexDocumentFromFileSystem(String sourcePath, List<String> keywords) {
        // Read the document from the file system
        var resource = new FileSystemResource(sourcePath);

        return processDocument(resource, keywords);
    }

    private Mono<List<Document>> processDocument(Resource resource, List<String> keywords) {
        Assert.isTrue(resource.exists(), "Resource does not exist");

        var parsedDocuments = tikaDocumentReader.readFrom(resource);
        var splitDocument = textSplitter.split(parsedDocuments);

        splitDocument.forEach(document -> addCustomMetadata(document, keywords));


        return Mono.fromRunnable(() -> {
                    log.debug("Calling vectorStore.add for {} documents on thread: {}", splitDocument.size(), Thread.currentThread().getName());
                    vectorStore.add(splitDocument);
                    log.debug("vectorStore.add completed for {} documents on thread: {}", splitDocument.size(), Thread.currentThread().getName());
                })
                .subscribeOn(Schedulers.boundedElastic()) // Ensures the Runnable runs on a dedicated thread
                .thenReturn(splitDocument) // After the Runnable completes, emit the splitDocument list
                .cache() // Caches the result of the Mono for subsequent subscriptions
                .doOnSuccess(splitDocuments -> log.info("Successfully added {} documents", splitDocuments.size()))
                .doOnError(e -> log.error("Error processing document: {}", e.getMessage(), e));

    }

    private void addCustomMetadata(Document document, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        Assert.notNull(document, "Document must not be null");

        document.getMetadata().put(CUSTOM_KEYWORDS_METADATA_KEY, keywords);
    }
}
