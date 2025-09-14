package com.rag.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSessionRequest {

    @Schema(description = "New session title. Null means no change. Blank should be rejected. Max 150 chars.",
            example = "Refining hybrid retrieval strategy")
    @Size(max = 150, message = "title exceeds maximum length 150")
    private String title;

    @Schema(description = "Mark or unmark the session as favorite. Null means no change.",
            example = "true")
    private Boolean favorite;
}