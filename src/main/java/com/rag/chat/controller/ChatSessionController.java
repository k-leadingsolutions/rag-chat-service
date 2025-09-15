package com.rag.chat.controller;

import com.rag.chat.dto.request.CreateSessionRequest;
import com.rag.chat.dto.request.UpdateSessionRequest;
import com.rag.chat.dto.response.PageResponse;
import com.rag.chat.dto.response.SessionResponse;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Sessions", description = "Endpoints for managing chat sessions")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sessions")
public class ChatSessionController {

    private final ChatSessionService service;

    @Operation(
            summary = "Create a chat session",
            security = {@SecurityRequirement(name = "bearer_jwt"), @SecurityRequirement(name = "api_key")}
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN','API_CLIENT')")
    @PostMapping
    public SessionResponse create(@Valid @RequestBody CreateSessionRequest req) {
        ChatSession session = service.create(req);
        if (log.isDebugEnabled()) {
            log.debug("Created chat session id={} ", session.getId());
        }
        return toResponse(session);
    }

    @Operation(
            summary = "List favorite chat sessions",
            description = "Optional filtering by favorite. Descending order by updatedAt. Page-based pagination.",
            security = {@SecurityRequirement(name = "bearer_jwt"), @SecurityRequirement(name = "api_key")}
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN','API_CLIENT')")
    @GetMapping
    public PageResponse<SessionResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ){
        int internalPage = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(internalPage, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ChatSession> sessions = service.list(pageable);

        if (log.isDebugEnabled()) {
            log.debug("Listed sessions count={} page={} size={} ",
                    sessions.getNumberOfElements(), page, size);
        }

        return PageResponse.from(sessions.map(this::toResponse));
    }

    @Operation(
            summary = "Get a specific chat session by ID",
            security = {@SecurityRequirement(name = "bearer_jwt"), @SecurityRequirement(name = "api_key")}
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN','API_CLIENT')")
    @GetMapping("/{id}")
    public SessionResponse get(@PathVariable UUID id) {
        ChatSession session = service.getOrThrow(id);
        if (log.isDebugEnabled()) {
            log.debug("Retrieved session id={} ", session.getId());
        }
        return toResponse(session);
    }

    @Operation(
            summary = "Update a chat session",
            security = {@SecurityRequirement(name = "bearer_jwt"), @SecurityRequirement(name = "api_key")}
    )
    @PreAuthorize("@sessionAccess.canWrite(authentication, #id)")
    @PatchMapping("/{id}")
    public SessionResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateSessionRequest req) {
        ChatSession updated = service.update(id, req);
        if (log.isDebugEnabled()) {
            log.debug("Updated session id={} title={} favorite={}", id, updated.getTitle(), updated.isFavorite());
        }
        return toResponse(updated);
    }

    @Operation(
            summary = "Delete a chat session",
            security = {@SecurityRequirement(name = "bearer_jwt"), @SecurityRequirement(name = "api_key")}
    )
    @PreAuthorize("@sessionAccess.canWrite(authentication, #id)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
        if (log.isDebugEnabled()) {
            log.debug("Deleted session id={} ", id);
        }
    }

    @Operation(
            summary = "Toggle favorite status of a session",
            security = {@SecurityRequirement(name = "bearer_jwt"), @SecurityRequirement(name = "api_key")}
    )
    @PreAuthorize("@sessionAccess.canWrite(authentication, #id)")
    @PutMapping("/{id}/favorite")
    public SessionResponse toggleFavorite(@PathVariable UUID id) {
        ChatSession updated = service.toggleFavorite(id);
        if (log.isDebugEnabled()) {
            log.debug("Toggled favorite for session id={} now favorite={}", id, updated.isFavorite());
        }
        return toResponse(updated);
    }

    private SessionResponse toResponse(ChatSession session) {
        return SessionResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .favorite(session.isFavorite())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}