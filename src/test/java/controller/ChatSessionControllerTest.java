package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.controller.ChatSessionController;
import com.rag.chat.dto.request.CreateSessionRequest;
import com.rag.chat.dto.request.UpdateSessionRequest;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.service.ChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatSessionControllerTest {

    @Mock
    private ChatSessionService chatSessionService;

    @InjectMocks
    private ChatSessionController chatSessionController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ChatSession chatSession;
    private UUID sessionId;
    private String userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatSessionController).build();

        sessionId = UUID.randomUUID();
        userId = "user-123";
        chatSession = new ChatSession();
        chatSession.setId(sessionId);
        chatSession.setTitle("Test Session");
        chatSession.setFavorite(true);
        chatSession.setCreatedAt(Instant.now());
        chatSession.setUpdatedAt(Instant.now());
    }

    @Test
    void create_shouldReturnSessionResponse() throws Exception {
        CreateSessionRequest req = new CreateSessionRequest();
        req.setTitle("Test Session");

        when(chatSessionService.create(any(CreateSessionRequest.class)))
                .thenReturn(chatSession);

        String requestJson = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.title").value("Test Session"))
                .andExpect(jsonPath("$.favorite").value(true));
    }

    @Test
    void list_shouldReturnPageResponse() throws Exception {
        Page<ChatSession> page = new PageImpl<>(List.of(chatSession),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "updatedAt")), 1);

        when(chatSessionService.list(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/sessions")
                        .param("userId", userId)
                        .param("favorite", "true")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(sessionId.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Test Session"))
                .andExpect(jsonPath("$.content[0].favorite").value(true));
    }

    @Test
    void get_shouldReturnSessionResponse() throws Exception {
        when(chatSessionService.getOrThrow(eq(sessionId))).thenReturn(chatSession);

        mockMvc.perform(get("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.title").value("Test Session"))
                .andExpect(jsonPath("$.favorite").value(true));
    }

    @Test
    void update_shouldReturnSessionResponse() throws Exception {
        UpdateSessionRequest req = new UpdateSessionRequest();
        req.setTitle("New Title");
        req.setFavorite(false);

        chatSession.setTitle("New Title");
        chatSession.setFavorite(false);

        when(chatSessionService.update(eq(sessionId), any(UpdateSessionRequest.class)))
                .thenReturn(chatSession);

        String requestJson = objectMapper.writeValueAsString(req);

        mockMvc.perform(patch("/api/v1/sessions/{id}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.favorite").value(false));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(chatSessionService).delete(eq(sessionId));

        mockMvc.perform(delete("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isNoContent());
    }
}