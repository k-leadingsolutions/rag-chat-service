package com.rag.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.rag.chat.enums.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id", "sessionId", "role", "content",
        "retrievedContext", "metadata", "createdAt"
})
@Schema(name = "MessageResponse")
public class MessageResponse {

    @Schema(description = "Message identifier", example = "3b1b8f86-cdc7-4b6a-9f4b-c5a871f4dc11")
    UUID id;

    @Schema(description = "Owning chat session identifier", example = "bf5cfb41-0c60-4b5b-8d7e-1c96b7e662b9")
    UUID sessionId;

    @Schema(description = "Message role (USER, ASSISTANT, SYSTEM, etc.)", example = "USER")
    SenderType role;

    @Schema(description = "Message textual content", example = "How can I improve retrieval chunking?")
    String content;

    @Schema(description = "Optional retrieved context JSON (RAG evidence bundle)")
    JsonNode retrievedContext;

    @Schema(description = "Optional arbitrary metadata JSON")
    JsonNode metadata;

    @Schema(description = "Creation timestamp (UTC instant)", example = "2025-09-12T11:39:01Z")
    Instant createdAt;
}