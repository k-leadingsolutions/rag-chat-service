package com.rag.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id", "title", "favorite", "createdAt", "updatedAt"
})
@Schema(name = "SessionResponse")
public class SessionResponse {

    @Schema(description = "Session identifier", example = "9f5c1bca-6b2b-4ec1-8c3c-0c2d6f0d52b7")
    UUID id;

    @Schema(description = "Session title (may be user-assigned or system-generated)", example = "Vector index tuning notes")
    String title;

    @Schema(description = "Favorite flag for quick access in UI. Defaults to false if omitted.",
            example = "true")
    boolean favorite;

    @Schema(description = "Creation timestamp (UTC instant)", example = "2025-09-12T11:40:05Z")
    Instant createdAt;

    @Schema(description = "Last update timestamp (UTC instant)", example = "2025-09-12T11:41:27Z")
    Instant updatedAt;
}