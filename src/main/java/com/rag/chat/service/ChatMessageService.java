package com.rag.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.aop.LogExecution;
import com.rag.chat.dto.request.CreateMessageRequest;
import com.rag.chat.entity.ChatMessage;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.exception.ResourceNotFoundException;
import com.rag.chat.repository.ChatMessageRepository;
import com.rag.chat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.rag.chat.util.JSONSerializerDeserializerUtil.serialize;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @CacheEvict(value = "chatMessages", allEntries = true)
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public ChatMessage create(UUID sessionId, CreateMessageRequest req) {
        SecurityService.sanitizeInput(req.getContent());
        ChatSession session = sessionRepository.findByIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (req.getRole() == null || req.getRole().name().isBlank()) {
            throw new IllegalArgumentException("Message role is required and cannot be blank");
        }
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("Message content is required and cannot be blank");
        }

        ChatMessage message = getChatMessage(req, session);

        message = messageRepository.save(message);
        if (!session.getUpdatedAt().equals(message.getCreatedAt())) {
            session.setUpdatedAt(message.getCreatedAt());
            sessionRepository.save(session);
        }

        log.info("Created message with ID: {} for session ID: {}", message.getId(), sessionId);
        return message;
    }

    /**
     * List chat message for a session
     * Cache results
     * @param sessionId
     * @param pageable
     * @return
     */
    @Cacheable(
            value = "chatMessages",
            key = "#sessionId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()",
            unless = "#result.isEmpty()"
    )
    @Transactional(readOnly = true)
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public Page<ChatMessage> list(UUID sessionId, Pageable pageable) {
        ChatSession session = sessionRepository.findByIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return messageRepository.findBySessionOrderByCreatedAtAsc(session, pageable);
    }

    /**
     * Map request to entity for processing
     * To-do: Implement Mapstruct for mappings
     * @param req
     * @param session
     * @return
     */
    private ChatMessage getChatMessage(CreateMessageRequest req, ChatSession session) {
        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setRole(req.getRole());
        message.setContent(req.getContent());
        message.setRetrievedContext(serialize(req.getRetrievedContext(), "retrievedContext"));
        message.setMetadata(serialize(req.getMetadata(), "metadata"));
        return message;
    }
}