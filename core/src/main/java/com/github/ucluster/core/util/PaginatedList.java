package com.github.ucluster.core.util;

import java.util.List;
import java.util.function.BiFunction;

public class PaginatedList<T> {
    private static int MAX_PER_PAGE = 100;
    private static int MIN_PER_PAGE = 10;

    private long size;
    private BiFunction<Integer, Integer, List<? extends T>> provider;

    public PaginatedList(long size, BiFunction<Integer, Integer, List<? extends T>> provider) {
        this.size = size;
        this.provider = provider;
    }

    public List<? extends T> page(int page, int perPage) {
        return provider.apply(getConsolidatedPage(page), getConsolidatedPerPage(perPage));
    }

    public long size() {
        return size;
    }

    public List<? extends T> toList() {
        return page(1, (int) size);
    }

    public Page<? extends T> toPage(int page, int perPage) {
        return new Page<>(size,
                getConsolidatedPage(page), getConsolidatedPerPage(perPage),
                provider.apply(getConsolidatedPage(page), getConsolidatedPerPage(perPage)));
    }

    private int getConsolidatedPage(int page) {
        return page < 1 ? 1 : page;
    }

    private int getConsolidatedPerPage(int perPage) {
        if (perPage > MAX_PER_PAGE) return MAX_PER_PAGE;
        if (perPage < MIN_PER_PAGE) return MIN_PER_PAGE;
        return perPage;
    }
}
