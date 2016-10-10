package com.github.ucluster.mongo.api.json;

import java.util.ArrayList;
import java.util.Collection;

public class ReferenceList<T> extends ArrayList<T> {
    public ReferenceList(int initialCapacity) {
        super(initialCapacity);
    }

    public ReferenceList() {
    }

    public ReferenceList(Collection<? extends T> c) {
        super(c);
    }
}
