package com.course.rag.infra.api.request;
import jakarta.validation.constraints.NotBlank;

import java.util.List;


public record AIRequest(
        String systemPrompt,
        @NotBlank String userPrompt
) {

}
