package com.course.rag.infra.api.response;

public record BasicIndexingResponse(
        boolean success,
        String message
) {
}
