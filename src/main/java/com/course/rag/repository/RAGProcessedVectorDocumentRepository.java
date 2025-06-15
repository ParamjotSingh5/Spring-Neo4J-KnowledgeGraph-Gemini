package com.course.rag.repository;

import com.course.rag.entity.RAGProcessedVectorDocument;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RAGProcessedVectorDocumentRepository extends R2dbcRepository<RAGProcessedVectorDocument, UUID> {

    Mono<RAGProcessedVectorDocument> findBySourcePath(String sourcePath);
}
