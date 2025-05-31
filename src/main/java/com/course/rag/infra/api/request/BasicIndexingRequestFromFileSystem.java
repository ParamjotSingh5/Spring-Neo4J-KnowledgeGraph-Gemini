package com.course.rag.infra.api.request;
import jakarta.validation.constraints.NotBlank;

import java.util.List;


public record BasicIndexingRequestFromFileSystem(
        @NotBlank String path,
        @NotBlank String outputFilename,
        Boolean appendIfFileExists,
        List<String> keywords
) {

}
