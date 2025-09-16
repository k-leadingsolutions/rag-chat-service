package com.rag.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "chat_session")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString
public class ChatSession extends BaseEntity {

    @Size(max = 150, message = "Title exceeds maximum length of 150 characters")
    private String title;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public void markDeleted() {
        this.deletedAt = Instant.now();
    }

    public void rename(String newTitle) {
        this.title = newTitle;
    }

    public void toggleFavorite(boolean value) {
        this.favorite = value;
    }
}