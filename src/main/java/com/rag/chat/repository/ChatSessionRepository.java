package com.rag.chat.repository;

import com.rag.chat.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    Optional<ChatSession> findByIdAndDeletedAtIsNull(UUID id);

    Page<ChatSession> findByFavoriteIsTrueAndDeletedAtIsNull(Pageable pageable);
}