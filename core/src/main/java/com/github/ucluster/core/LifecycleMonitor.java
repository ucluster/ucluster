package com.github.ucluster.core;

public interface LifecycleMonitor<T extends Record> {

    T monitor(T record);
}
