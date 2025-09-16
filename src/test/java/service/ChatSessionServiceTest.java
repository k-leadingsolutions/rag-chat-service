package service;

import com.rag.chat.dto.request.CreateSessionRequest;
import com.rag.chat.dto.request.UpdateSessionRequest;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.exception.ResourceNotFoundException;
import com.rag.chat.repository.ChatSessionRepository;
import com.rag.chat.service.ChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatSessionServiceTest {

    @Mock
    private ChatSessionRepository repository;


    @InjectMocks
    private ChatSessionService chatSessionService;

    private UUID sessionId;
    private ChatSession chatSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionId = UUID.randomUUID();
        chatSession = new ChatSession();
        chatSession.setId(sessionId);
        chatSession.setTitle("Test Session");
        chatSession.setFavorite(true);
        chatSession.setCreatedAt(Instant.now());
        chatSession.setUpdatedAt(Instant.now());

    }

    @Test
    void create_shouldSaveAndReturnSession() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.setTitle("Test Session");

        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> {
            ChatSession s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ChatSession result = chatSessionService.create(req);

        assertNotNull(result.getId());
        assertEquals("Test Session", result.getTitle());
        verify(repository, times(1)).save(any(ChatSession.class));
    }


    @Test
    void getOrThrow_shouldReturnSessionIfExists() {
        when(repository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.of(chatSession));
        ChatSession result = chatSessionService.getOrThrow(sessionId);
        assertEquals(chatSession, result);
    }

    @Test
    void getOrThrow_shouldThrowIfNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chatSessionService.getOrThrow(sessionId));
    }

    @Test
    void list_shouldReturnFavoriteSessionsIfFavoriteTrue() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatSession> page = new PageImpl<>(List.of(chatSession), pageable, 1);

        when(repository.findByFavoriteIsTrueAndDeletedAtIsNull(pageable)).thenReturn(page);

        Page<ChatSession> result = chatSessionService.list(pageable);

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findByFavoriteIsTrueAndDeletedAtIsNull(pageable);
    }

    @Test
    void update_shouldUpdateTitleAndFavorite() {
        UpdateSessionRequest req = new UpdateSessionRequest();
        req.setTitle("Updated Title");
        req.setFavorite(false);

        when(repository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.of(chatSession));
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSession result = chatSessionService.update(sessionId, req);

        assertEquals("Updated Title", result.getTitle());
        assertFalse(result.isFavorite());
        verify(repository, times(1)).save(chatSession);
    }

    @Test
    void update_shouldUpdateOnlyFieldsPresent() {
        UpdateSessionRequest req = new UpdateSessionRequest();
        req.setTitle(null);
        req.setFavorite(true);

        when(repository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.of(chatSession));
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatSession.setFavorite(false);
        ChatSession result = chatSessionService.update(sessionId, req);

        assertEquals("Test Session", result.getTitle());
        assertTrue(result.isFavorite());
    }

    @Test
    void update_shouldThrowIfSessionNotFound() {
        UpdateSessionRequest req = new UpdateSessionRequest();
        req.setTitle("Updated Title");

        when(repository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatSessionService.update(sessionId, req));
    }

    @Test
    void delete_shouldSetDeletedAt() {
        when(repository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.of(chatSession));
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatSessionService.delete(sessionId);

        assertNotNull(chatSession.getDeletedAt());
        verify(repository, times(1)).save(chatSession);
    }

    @Test
    void delete_shouldThrowIfSessionNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(sessionId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chatSessionService.delete(sessionId));
    }
}
