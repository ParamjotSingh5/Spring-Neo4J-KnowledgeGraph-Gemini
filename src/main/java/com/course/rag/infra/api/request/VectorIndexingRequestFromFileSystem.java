package com.course.rag.infra.api.request;
import jakarta.validation.constraints.NotBlank;

import java.util.List;


public record VectorIndexingRequestFromFileSystem(
        @NotBlank String path,
        List<String> keywords
) {

}
