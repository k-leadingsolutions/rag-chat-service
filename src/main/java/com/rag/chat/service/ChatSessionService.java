package com.rag.chat.service;

import com.rag.chat.aop.LogExecution;
import com.rag.chat.dto.request.CreateSessionRequest;
import com.rag.chat.dto.request.UpdateSessionRequest;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.repository.ChatSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ChatSessionService {

    private final ChatSessionRepository repository;

    public ChatSessionService(ChatSessionRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a session C-Create, R, U, D)
     * @param req
     * @return
     */
    @Transactional
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public ChatSession create(CreateSessionRequest req) {
        ChatSession session = new ChatSession();
        SecurityService.sanitizeInput(req.getTitle());
        session.setTitle(req.getTitle() != null ? req.getTitle() : "New Chat (created at " + Instant.now().toString() + ")");
        return repository.save(session);
    }

    /**
     * Retrieve a session
     * or else entity not found exception
     * @param id
     * @return
     */
    public ChatSession getOrThrow(UUID id){
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
    }

    /**
     * Retrieve active favorite sessions
     * Cache results
     * @param pageable
     * @return
     */
    @Cacheable(
            value = "sessions",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()",
            unless = "#result.isEmpty()"
    )
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public Page<ChatSession> list(Pageable pageable) {
            return repository.findByFavoriteIsTrueAndDeletedAtIsNull(pageable);
    }

    /**
     * Update a session
     * @param id
     * @param req
     * @return
     */
    @Transactional
    @CachePut(value = "sessions", key = "#session.id")
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public ChatSession update(UUID id, UpdateSessionRequest req){
        ChatSession session = getOrThrow(id);
        SecurityService.sanitizeInput(req.getTitle());
        if (req.getTitle() != null) session.setTitle(req.getTitle());
        if (req.getFavorite() != null) session.setFavorite(req.getFavorite());
        return repository.save(session);
    }

    /**
     * Soft-delete a session
     * Evict Cache
     * @param id
     */
    @Transactional
    @CacheEvict(value = "sessions", key = "#sessionId")
    public void delete(UUID id){
        ChatSession session = getOrThrow(id);
        session.setDeletedAt(Instant.now());
        repository.save(session);
    }
}