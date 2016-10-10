package com.github.ucluster.mongo;

import java.util.Map;

public interface Model {
    Map<String, Object> toJson();

    Map<String, Object> toReferenceJson();
}
