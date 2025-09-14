package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.aop.LogExecution;
import com.rag.chat.dto.request.CreateMessageRequest;
import com.rag.chat.entity.ChatMessage;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.exception.ResourceNotFoundException;
import com.rag.chat.repository.ChatMessageRepository;
import com.rag.chat.repository.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChatMessageService {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageService.class);

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    public ChatMessageService(ChatMessageRepository messageRepository,
                              ChatSessionRepository sessionRepository,
                              ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @LogExecution(includeArgs = true, includeResult = false, warnThresholdMs = 500)
    public ChatMessage create(UUID sessionId, CreateMessageRequest req) {
        ChatSession session = sessionRepository.findByIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (req.getRole() == null || req.getRole().name().isBlank()) {
            throw new IllegalArgumentException("Message role is required and cannot be blank");
        }
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("Message content is required and cannot be blank");
        }

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setRole(req.getRole());
        message.setContent(req.getContent());
        message.setRetrievedContext(serialize(req.getRetrievedContext(), "retrievedContext"));
        message.setMetadata(serialize(req.getMetadata(), "metadata"));

        message = messageRepository.save(message);
        if (!session.getUpdatedAt().equals(message.getCreatedAt())) {
            session.setUpdatedAt(message.getCreatedAt());
            sessionRepository.save(session);
        }

        log.info("Created message with ID: {} for session ID: {}", message.getId(), sessionId);
        return message;
    }

    public Page<ChatMessage> list(UUID sessionId, Pageable pageable) {
        ChatSession session = sessionRepository.findByIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return messageRepository.findBySessionOrderByCreatedAtAsc(session, pageable);
    }

    private String serialize(Object obj, String fieldName) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize {} field: {}", fieldName, e.getMessage());
            return null;
        }
    }
}