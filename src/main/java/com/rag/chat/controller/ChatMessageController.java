package com.rag.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.dto.request.CreateMessageRequest;
import com.rag.chat.dto.response.MessageResponse;
import com.rag.chat.dto.response.PageResponse;
import com.rag.chat.entity.ChatMessage;
import com.rag.chat.service.ChatMessageService;
import com.rag.chat.util.JSONSerializerDeserializerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Messages", description = "Endpoints for managing chat messages")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
public class ChatMessageController {

    private final ChatMessageService service;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "Create a message in a session",
            security = { @SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "internalApiKey") }
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN','API_CLIENT')")
    @PostMapping
    public MessageResponse create(@PathVariable UUID sessionId,
                                  @Valid @RequestBody CreateMessageRequest req) {
        ChatMessage message = service.create(sessionId, req);
        if (log.isDebugEnabled()) {
            log.debug("Created message id={} sessionId={} role={}", message.getId(), sessionId, message.getRole());
        }
        return toResponse(message, true, true);
    }

    @Operation(
            summary = "List messages for a session",
            description = "Ascending order by createdAt; page-based pagination",
            security = { @SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "internalApiKey") }
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN','API_CLIENT')")
    @GetMapping
    public PageResponse<MessageResponse> list(@PathVariable UUID sessionId,
                                              @RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "20") @Min(0) @Max(100) int size,
                                              @Parameter(description = "If false, omit retrievedContext JSON") @RequestParam(defaultValue = "true") boolean includeContext,
                                              @Parameter(description = "If false, omit metadata JSON") @RequestParam(defaultValue = "true") boolean includeMetadata) {
        int internalPage = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(internalPage, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        if (log.isDebugEnabled()) {
            log.debug("Listing messages sessionId={} page={} size={} includeContext={} includeMetadata={}",
                    sessionId, page, size, includeContext, includeMetadata);
        }

        Page<ChatMessage> messages = service.list(sessionId, pageable);

        Page<MessageResponse> dtoPage = messages.map(m -> toResponse(m, includeContext, includeMetadata));

        return PageResponse.from(dtoPage);
    }

    private MessageResponse toResponse(ChatMessage m, boolean includeContext, boolean includeMetadata) {
        UUID sessionId = m.getSession() != null ? m.getSession().getId() : null;
        return MessageResponse.builder()
                .id(m.getId())
                .sessionId(sessionId)
                .role(m.getRole())
                .content(m.getContent())
                .retrievedContext(includeContext ? JSONSerializerDeserializerUtil.deserialize(m.getRetrievedContext()) : null)
                .metadata(includeMetadata ? JSONSerializerDeserializerUtil.deserialize(m.getMetadata().toString()) : null)
                .createdAt(m.getCreatedAt())
                .build();
    }

}