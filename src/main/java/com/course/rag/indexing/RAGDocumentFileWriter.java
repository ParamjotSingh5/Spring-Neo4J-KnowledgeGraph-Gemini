package com.course.rag.indexing;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.stereotype.Component;

import org.springframework.ai.document.Document;

import java.util.List;

@Component
public class RAGDocumentFileWriter {

    public void writeDocumentsToFile(List<Document> documents, String fileName, boolean appendIfFileExists) {
        var writer = new FileDocumentWriter(fileName, true, MetadataMode.ALL, appendIfFileExists);

        writer.accept(documents);
    }
}
