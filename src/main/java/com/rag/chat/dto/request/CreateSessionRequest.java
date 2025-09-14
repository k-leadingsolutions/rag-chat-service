package com.rag.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSessionRequest {

    @Schema(
            description = "Optional session title. If omitted the service may generate a default.",
            example = "Exploring retrieval strategies"
    )
    @Size(max = 150, message = "title exceeds maximum length 150")
    private String title;
}