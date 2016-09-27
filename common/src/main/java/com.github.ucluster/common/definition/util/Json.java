package com.github.ucluster.common.definition.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Map;

public class Json {
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final TypeReference<Map<String, Object>> JSON_TYPE = new TypeReference<Map<String, Object>>() {
    };

    static {
        mapper.registerModule(new JodaModule());
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.
                WRITE_DATES_AS_TIMESTAMPS, true);
    }

    public static String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(Blob blob, TypeReference<T> type) {
        try {
            return mapper.readValue(blob.getBinaryStream(), type);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String string, TypeReference<T> type) {
        try {
            return mapper.readValue(string, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> fromJson(String json) {
        try {
            return mapper.readValue(json, JSON_TYPE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
