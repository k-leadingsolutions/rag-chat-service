package com.rag.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Page;

import java.util.List;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    List<T> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean hasNext;
    boolean hasPrevious;

    public static <T> PageResponse<T> from(Page<T> p) {
        return PageResponse.<T>builder()
                .content(p.getContent())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .hasNext(p.hasNext())
                .hasPrevious(p.hasPrevious())
                .build();
    }
}