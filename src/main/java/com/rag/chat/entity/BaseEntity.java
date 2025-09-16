package com.rag.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    protected UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    @Version
    @Column(nullable = false)
    protected Long version;
}