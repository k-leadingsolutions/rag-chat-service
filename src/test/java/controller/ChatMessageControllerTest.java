package controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.controller.ChatMessageController;
import com.rag.chat.dto.request.CreateMessageRequest;
import com.rag.chat.dto.response.MessageResponse;
import com.rag.chat.entity.ChatMessage;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.entity.SenderType;
import com.rag.chat.service.ChatMessageService;
import com.rag.chat.util.JSONSerializerDeserializerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.rag.chat.util.JSONSerializerDeserializerUtil.deserialize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatMessageController chatMessageController;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private UUID sessionId;
    private ChatMessage chatMessage;
    private ChatSession chatSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatMessageController).build();

        sessionId = UUID.randomUUID();
        chatSession = new ChatSession();
        chatSession.setId(sessionId);

        chatMessage = new ChatMessage();
        chatMessage.setId(UUID.randomUUID());
        chatMessage.setSession(chatSession);
        chatMessage.setRole(SenderType.SYSTEM);
        chatMessage.setContent("Hello!");
        chatMessage.setRetrievedContext("{\"foo\":\"bar\"}");
        chatMessage.setMetadata("{\"foo\":\"bar\"}");
        chatMessage.setCreatedAt(Instant.now());
    }

    @Test
    void create_shouldReturnMessageResponse() throws Exception {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setContent("Hello!");
        req.setRole(SenderType.SYSTEM);

        when(chatMessageService.create(eq(sessionId), any(CreateMessageRequest.class)))
                .thenReturn(chatMessage);

        String requestJson = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/v1/sessions/{sessionId}/messages", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chatMessage.getId().toString()))
                .andExpect(jsonPath("$.role").value("SYSTEM"))
                .andExpect(jsonPath("$.content").value("Hello!"))
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.retrievedContext.foo").value("bar"));
    }
    @Test
    void list_shouldReturnPageResponse() throws Exception {
        Page<ChatMessage> page = new PageImpl<>(List.of(chatMessage), 
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "createdAt")), 1);

        when(chatMessageService.list(eq(sessionId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/sessions/{sessionId}/messages", sessionId)
                .param("page", "0")
                .param("size", "50")
                .param("includeContext", "true")
                .param("includeMetadata", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void toResponse_shouldRespectIncludeFlags() {

        MessageResponse resp1 = toResponse(chatMessage, true, true);
        assertNotNull(resp1.getRetrievedContext());
        assertNotNull(resp1.getMetadata());

        MessageResponse messageResponse = MessageResponse.builder().build();

        MessageResponse resp2 = toResponse(chatMessage, false, false);
        assertNull(resp2.getRetrievedContext());
        assertNull(resp2.getMetadata());
    }

    @Test
    void deserialize_shouldReturnNullForInvalidJson() {
        ChatMessageController controller = new ChatMessageController(chatMessageService);
        String invalidJson = "{ invalid json }";
        JsonNode node = deserialize(invalidJson);
        assertNull(node);
    }


    private MessageResponse toResponse(ChatMessage m, boolean includeContext, boolean includeMetadata) {
        UUID sessionId = m.getSession() != null ? m.getSession().getId() : null;
        return MessageResponse.builder()
                .id(m.getId())
                .sessionId(sessionId)
                .role(m.getRole())
                .content(m.getContent())
                .retrievedContext(includeContext ? deserialize(m.getRetrievedContext()) : null)
                .metadata(includeMetadata ? JSONSerializerDeserializerUtil.deserialize(m.getMetadata()) : null)
                .createdAt(m.getCreatedAt())
                .build();
    }
}