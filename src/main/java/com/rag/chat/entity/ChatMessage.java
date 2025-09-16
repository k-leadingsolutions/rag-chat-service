package com.rag.chat.entity;

import com.rag.chat.enums.SenderType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_message",
        indexes = {
                @Index(name = "idx_chat_message_session_created", columnList = "session_id, created_at")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(exclude = {"session", "retrievedContext", "metadata"})
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_message_session"))
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SenderType role;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "retrieved_context", columnDefinition = "TEXT")
    private String retrievedContext;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    public static ChatMessage of(ChatSession session, SenderType role, String content) {
        return ChatMessage.builder()
                .session(session)
                .role(role)
                .content(content)
                .build();
    }
}