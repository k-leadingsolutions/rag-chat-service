package service;

import com.rag.chat.dto.request.CreateMessageRequest;
import com.rag.chat.entity.ChatMessage;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.enums.SenderType;
import com.rag.chat.exception.ResourceNotFoundException;
import com.rag.chat.repository.ChatMessageRepository;
import com.rag.chat.repository.ChatSessionRepository;
import com.rag.chat.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private ChatSessionRepository sessionRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private UUID sessionId;
    private ChatSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionId = UUID.randomUUID();
        session = new ChatSession();
        session.setId(sessionId);
        session.setUpdatedAt(Instant.now());
    }

    @Test
    void testCreateMessage_Success() {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setRole(SenderType.USER);
        req.setContent("Hello");

        when(sessionRepository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.of(session));

        ChatMessage savedMessage = new ChatMessage();
        savedMessage.setId(UUID.randomUUID());
        savedMessage.setCreatedAt(Instant.now());

        when(messageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        ChatMessage result = chatMessageService.create(sessionId, req);

        assertNotNull(result);
        assertEquals(savedMessage.getId(), result.getId());

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("Hello", captor.getValue().getContent());
    }

    @Test
    void testCreateMessage_SessionNotFound() {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setRole(SenderType.USER);
        req.setContent("Hello");

        when(sessionRepository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatMessageService.create(sessionId, req));
    }

    @Test
    void testCreateMessage_RoleRequired() {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setContent("Hello");

        when(sessionRepository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.of(session));

        assertThrows(IllegalArgumentException.class, () -> chatMessageService.create(sessionId, req));
    }

    @Test
    void testCreateMessage_ContentRequired() {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setRole(SenderType.USER);

        when(sessionRepository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.of(session));

        assertThrows(IllegalArgumentException.class, () -> chatMessageService.create(sessionId, req));
    }


    @Test
    void testListMessages_Success() {
        Pageable pageable = mock(Pageable.class);
        when(sessionRepository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.of(session));

        ChatMessage message = new ChatMessage();
        Page<ChatMessage> page = new PageImpl<>(java.util.List.of(message));
        when(messageRepository.findBySessionOrderByCreatedAtAsc(session, pageable)).thenReturn(page);

        Page<ChatMessage> result = chatMessageService.list(sessionId, pageable);

        assertFalse(result.isEmpty());
        verify(messageRepository).findBySessionOrderByCreatedAtAsc(session, pageable);
    }
}