package com.course.rag.service;

import com.course.rag.entity.RAGProcessedVectorDocument;
import com.course.rag.entity.RAGProcessedVectorDocumentChunk;
import com.course.rag.indexing.RAGTikaDocumentReader;
import com.course.rag.repository.RAGProcessedVectorDocumentChunksRepository;
import com.course.rag.repository.RAGProcessedVectorDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.autoconfigure.vectorstore.neo4j.Neo4jVectorStoreProperties;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RAGVectorIndexingService {

    @Autowired
    private TextSplitter textSplitter;

    @Autowired
    private RAGTikaDocumentReader tikaDocumentReader;

    @Autowired
    private Neo4jVectorStore vectorStore;

    @Autowired
    Neo4jVectorStoreProperties neo4jVectorStoreProperties;

    @Autowired
    private RAGProcessedVectorDocumentRepository processedVectorDocumentRepository;

    @Autowired
    private RAGProcessedVectorDocumentChunksRepository processedVectorDocumentChunkRepository;

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
                    log.debug("embedding dimension: {}", neo4jVectorStoreProperties.getEmbeddingDimension());
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

    private String calculateHash(Resource resource) {
        var lastModified = 0L;

        try {
            lastModified = resource.lastModified();
        } catch (Exception e) {
            log.error("Error getting last modified time for resource: {}", e.getMessage());
        }

        var original = resource.getDescription().toLowerCase() + "//" + lastModified;

        return DigestUtils.sha256Hex(original);
    }

    @SuppressWarnings("null")
    private Mono<List<Document>> processDocumentReactive(Resource resource, List<String> keywords) {
        Assert.isTrue(resource != null && resource.exists(), "Resource must not be null and must exist");

        var existingFromDb = processedVectorDocumentRepository.findBySourcePath(resource.getDescription());
        var now = OffsetDateTime.now();

        return existingFromDb
                .defaultIfEmpty(
                        new RAGProcessedVectorDocument(null, resource.getDescription(), StringUtils.EMPTY, now, now))
                .flatMap(element -> {
                    var hash = calculateHash(resource);

                    if (StringUtils.equals(element.getHash(), hash)) {
                        log.info("Document with hash {} already indexed", hash);

                        return Mono.empty();
                    }

                    var parsedDocuments = tikaDocumentReader.readFrom(resource);
                    var splittedDocuments = textSplitter.split(parsedDocuments);

                    splittedDocuments.forEach(document -> addCustomMetadata(document, keywords));

                    vectorStore.add(splittedDocuments);

                    log.info("Original document splitted into {} chunks and saved to vector store",
                            splittedDocuments.size());

                    element.setHash(hash);
                    element.setUpdatedAt(now);

                    try {
                        processedVectorDocumentChunkRepository
                                .findByProcessedVectorDocumentId(element.getProcessedVectorDocumentId())
                                .subscribe(chunk -> {
                                    processedVectorDocumentChunkRepository.deleteById(chunk.getProcessedVectorDocumentId()).subscribe();
                                    vectorStore.delete(List.of(chunk.getChunkId()));
                                });

                        processedVectorDocumentRepository.save(element).subscribe(
                                savedDocument -> {
                                    splittedDocuments.forEach(chunk -> {
                                        var chunkEntity = new RAGProcessedVectorDocumentChunk(chunk.getId(),
                                                savedDocument.getProcessedVectorDocumentId());

                                        processedVectorDocumentChunkRepository.save(chunkEntity).subscribe();
                                    });
                                });
                    } catch (Exception e) {
                        log.error("Failed to save processed document to database", e);
                        return Mono.empty();
                    }

                    return Mono.just(splittedDocuments);
                });
    }
}
