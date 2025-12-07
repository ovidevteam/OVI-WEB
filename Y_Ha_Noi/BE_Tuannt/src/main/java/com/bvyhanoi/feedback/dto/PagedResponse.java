package com.bvyhanoi.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private Long total;
    private Integer page;
    private Integer size;
    
    public static <T> PagedResponse<T> of(List<T> data, Long total, Integer page, Integer size) {
        return new PagedResponse<>(data, total, page, size);
    }
}

