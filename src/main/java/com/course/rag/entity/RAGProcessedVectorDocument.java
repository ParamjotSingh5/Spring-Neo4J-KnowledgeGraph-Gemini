package com.course.rag.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("rag_processed_vector_documents")
@Getter
@Setter
public class RAGProcessedVectorDocument implements Persistable<UUID> {
    @Id
    private UUID processedVectorDocumentId;

    private String sourcePath;

    private String hash;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public RAGProcessedVectorDocument(UUID processedVectorDocumentId, String sourcePath, String hash, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.processedVectorDocumentId = processedVectorDocumentId;
        this.sourcePath = sourcePath;
        this.hash = hash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RAGProcessedVectorDocument() {
    }


    @Override
    public UUID getId() {
        if(this.processedVectorDocumentId == null){
            this.processedVectorDocumentId = UUID.randomUUID();
        }

        return this.processedVectorDocumentId;
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
