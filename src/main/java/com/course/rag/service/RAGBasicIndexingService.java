package com.course.rag.service;

import com.course.rag.indexing.RAGDocumentFileWriter;
import com.course.rag.indexing.RAGTikaDocumentReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
@Slf4j
public class RAGBasicIndexingService {

    @Autowired
    private TextSplitter textSplitter;

    @Autowired
    private RAGTikaDocumentReader tikaDocumentReader;

    @Autowired
    private RAGDocumentFileWriter ragDocumentFileWriter;

    private static final String CUSTOM_KEYWORDS_METADATA_KEY = "custom_keywords";


    public List<Document> indexDocumentFromURL(String sourcePath, String outputFilename, boolean appendIfFileExists, List<String> keywords) {
        // Read the document from the URL
        try{
            var resource =  new UrlResource(sourcePath);
            return processDocument(resource, outputFilename, appendIfFileExists, keywords);
        }
        catch (Exception e){
            log.error("Error in indexDocumentFromURL: {}", e.getMessage());
            throw new RuntimeException("Error in indexDocumentFromURL: " + e.getMessage());
        }
    }

    public List<Document> indexDocumentFromFileSystem(String sourcePath, String outputFilename, boolean appendIfFileExists, List<String> keywords) {
        // Read the document from the file system
        var resource = new FileSystemResource(sourcePath);

        return processDocument(resource, outputFilename, appendIfFileExists, keywords);
    }

    private List<Document> processDocument(Resource resource, String outputFilename, boolean appendIfFileExists, List<String> keywords) {
        Assert.isTrue(resource.exists(), "Resource does not exist");

        var parsedDocuments = tikaDocumentReader.readFrom(resource);
        var splitDocument = textSplitter.split(parsedDocuments);

        splitDocument.forEach(document -> addCustomMetadata(document, keywords));

        ragDocumentFileWriter.writeDocumentsToFile(splitDocument, outputFilename, appendIfFileExists);

        return splitDocument;
    }

    private void addCustomMetadata(Document document, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        Assert.notNull(document, "Document must not be null");

        document.getMetadata().put(CUSTOM_KEYWORDS_METADATA_KEY, keywords);
    }
}
