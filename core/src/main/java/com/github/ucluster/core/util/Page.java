package com.github.ucluster.core.util;

import java.util.List;

public class Page<T> {
    private long totalEntriesCount;
    private long page;
    private long perPage;
    private List<T> entries;

    public Page(long totalEntriesCount, long page, long perPage, List<T> entries) {
        this.totalEntriesCount = totalEntriesCount;
        this.page = page;
        this.perPage = perPage;
        this.entries = entries;
    }

    public long getPage() {
        return page;
    }

    public long getTotalPagesCount() {
        return (totalEntriesCount - 1) / perPage + 1;
    }

    public long getTotalEntriesCount() {
        return totalEntriesCount;
    }

    public long getPerPage() {
        return perPage;
    }

    public List<T> getEntries() {
        return entries;
    }
}
