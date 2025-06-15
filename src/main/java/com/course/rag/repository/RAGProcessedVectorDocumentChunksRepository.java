package com.course.rag.repository;

import com.course.rag.entity.RAGProcessedVectorDocument;
import com.course.rag.entity.RAGProcessedVectorDocumentChunk;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RAGProcessedVectorDocumentChunksRepository extends R2dbcRepository<RAGProcessedVectorDocumentChunk, UUID> {

    Flux<RAGProcessedVectorDocumentChunk> findByProcessedVectorDocumentId(UUID processedVectorDocumentId);
}
