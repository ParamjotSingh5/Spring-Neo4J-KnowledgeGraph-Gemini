package com.course.rag.infra.api.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;


public record VectorIndexingRequestFromURl(
        @NotBlank @Pattern(regexp = "^(?i)(http|https)://.*$") String url,
        List<String> keywords
) {

}
