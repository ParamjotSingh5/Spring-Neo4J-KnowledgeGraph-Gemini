package com.course.rag.entity;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("rag_processed_vector_document_chunks")
public class RAGProcessedVectorDocumentChunk implements Persistable<String > {

    @Id
    private String chunkId;
    private UUID processedDocumentId;

    @Override
    @Nullable
    public String getId(){
        if(this.chunkId == null){
            this.chunkId = UUID.randomUUID().toString();
        }

        return this.chunkId;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
