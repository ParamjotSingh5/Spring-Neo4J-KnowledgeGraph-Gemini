package com.course.rag.indexing;


import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RAGTikaDocumentReader {

    public List<Document> readFrom(Resource resource) {
        var TikaDocumentReader =  new TikaDocumentReader(resource);

        return TikaDocumentReader.read();
    }
}
