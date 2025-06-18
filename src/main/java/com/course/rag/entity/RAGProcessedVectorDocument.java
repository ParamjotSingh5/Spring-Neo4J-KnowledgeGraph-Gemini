package com.course.rag.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@Table("rag_processed_vector_documents")
public class RAGProcessedVectorDocument implements Persistable<UUID> {

    @Id
    private UUID processedDocumentId;

    private String sourcePath;

    private String hash;

    private OffsetDateTime firstProcessedAt;

    private OffsetDateTime lastProcessedAt;

    public RAGProcessedVectorDocument() {

    }

    public RAGProcessedVectorDocument(UUID processedDocumentId, String sourcePath, String hash,
                                      OffsetDateTime firstProcessedAt, OffsetDateTime lastProcessedAt) {
        this.processedDocumentId = processedDocumentId;
        this.sourcePath = sourcePath;
        this.hash = hash;
        this.firstProcessedAt = firstProcessedAt;
        this.lastProcessedAt = lastProcessedAt;
    }

    @Override
    public String toString() {
        return "RAGProcessedVectorDocument [processedDocumentId=" + processedDocumentId + ", sourcePath=" + sourcePath
                + ", hash=" + hash + ", firstProcessedAt=" + firstProcessedAt + ", lastProcessedAt=" + lastProcessedAt
                + "]";
    }

    @Override
    @Nullable
    public UUID getId() {
        if (this.processedDocumentId == null) {
            this.processedDocumentId = UUID.randomUUID();
        }

        return this.processedDocumentId;
    }

    @Override
    public boolean isNew() {
        return this.processedDocumentId == null;
    }
}
