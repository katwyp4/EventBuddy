package com.example.myapplication.model;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> content;
    private int totalPages;
    private int number;
    private int size;

    private long totalElements;
    private boolean last;
    private boolean first;
    private boolean empty;


    public List<T> getContent() {
        return content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isEmpty() {
        return empty;
    }

}
