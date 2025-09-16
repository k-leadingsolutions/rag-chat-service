package com.rag.chat.service;

import com.rag.chat.aop.LogExecution;
import com.rag.chat.dto.request.CreateSessionRequest;
import com.rag.chat.dto.request.UpdateSessionRequest;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.exception.ResourceNotFoundException;
import com.rag.chat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatSessionService {

    private final ChatSessionRepository repository;
    private final MessageSource messageSource;

    private String msg() {
        return messageSource.getMessage("session.default.title", null, "session.default.title", LocaleContextHolder.getLocale());
    }

    /**
     * Create a new chat session
     */
    @Transactional
    @CacheEvict(value = "sessionPages", allEntries = true)
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public ChatSession create(CreateSessionRequest req) {
        SecurityService.sanitizeInput(req.getTitle());

        ChatSession session = new ChatSession();
        session.setTitle(req.getTitle() != null
                ? req.getTitle()
                : msg() + " " + Instant.now());

        return repository.save(session);
    }

    /**
     * Get session by ID or throw ResourceNotFoundException
     */
    public ChatSession getOrThrow(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("session.not.found")); // changed here
    }

    /**
     * List favorite sessions with pagination
     */
    @Cacheable(
            value = "sessionPages",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()",
            unless = "#result.isEmpty()"
    )
    @Transactional(readOnly = true)
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public Page<ChatSession> list(Pageable pageable) {
        return repository.findByFavoriteIsTrueAndDeletedAtIsNull(pageable);
    }

    /**
     * Toggle favorite flag
     */
    @Transactional
    @CacheEvict(value = "sessionPages", allEntries = true)
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public ChatSession toggleFavorite(UUID id) {
        ChatSession session = getOrThrow(id);
        session.setFavorite(!session.isFavorite());
        return repository.save(session);
    }

    /**
     * Update session
     */
    @Transactional
    @CacheEvict(value = "sessionPages", allEntries = true)
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public ChatSession update(UUID id, UpdateSessionRequest req) {
        ChatSession session = getOrThrow(id);
        SecurityService.sanitizeInput(req.getTitle());

        if (req.getTitle() != null) session.setTitle(req.getTitle());
        if (req.getFavorite() != null) session.setFavorite(req.getFavorite());

        return repository.save(session);
    }

    /**
     * Soft delete session
     */
    @Transactional
    @CacheEvict(value = "sessionPages", allEntries = true)
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public void delete(UUID id) {
        ChatSession session = getOrThrow(id);
        session.setDeletedAt(Instant.now());
        repository.save(session);
    }
}