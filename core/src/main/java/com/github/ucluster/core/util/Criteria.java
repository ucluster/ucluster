package com.github.ucluster.core.util;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

public class Criteria {
    private Map<String, Collection<String>> criteria = new HashMap<>();

    public Criteria() {
    }

    public static Criteria empty() {
        return new Criteria();
    }

    public Criteria param(String key, String value) {
        Collection<String> values = criteria.get(key);

        if (values == null) {
            values = new HashSet<>();
            criteria.put(key, values);
        }
        values.add(value);
        return this;
    }

    public Collection<String> param(String key) {
        return criteria.get(key);
    }

    public static Criteria params() {
        return new Criteria();
    }

    public void params(Consumer<Map.Entry<String, Collection<String>>> consumer) {
        criteria.entrySet().stream().forEach(consumer);
    }
}
