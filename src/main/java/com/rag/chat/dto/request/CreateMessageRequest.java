package com.rag.chat.dto.request;

import com.rag.chat.entity.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMessageRequest {

    @Schema(description = "Role of the message origin (USER, ASSISTANT, or SYSTEM)", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "USER")
    @NotNull(message = "role is required")
    private SenderType role;

    @Schema(description = "Message content text", example = "Hello, how can I improve retrieval?")
    @NotBlank(message = "content must not be blank")
    @Size(max = 4000, message = "content exceeds maximum length (4000)")
    private String content;

    @Schema(description = "Optional contextual retrieval payload (arbitrary JSON structure)", example = """
            {"chunks":[{"id":"doc-1#p3","score":0.89,"text":"..."}]}""")
    private Object retrievedContext;

    @Schema(description = "Optional arbitrary metadata. Not enforced at this stage.", nullable = true, example = """
            {"client":"web","traceId":"abc-123"}""")
    private Object metadata;
}