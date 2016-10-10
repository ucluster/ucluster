package com.github.ucluster.mongo.api.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.joda.PackageVersion;
import com.github.ucluster.mongo.Model;
import org.joda.time.DateTime;

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
        mapper.registerModule(new JodaTimeModule());
        mapper.registerModule(new ReferenceListModule());
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

    static class JodaTimeModule extends SimpleModule {
        public JodaTimeModule() {
            super(PackageVersion.VERSION);
            addSerializer(DateTime.class, new JsonSerializer<DateTime>() {
                @Override
                public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                    jgen.writeNumber(value.getMillis());
                }
            });

            addDeserializer(DateTime.class, new JsonDeserializer<DateTime>() {
                @Override
                public DateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                    String value = jp.readValueAs(String.class);
                    return new DateTime(Long.parseLong(String.valueOf(value)));
                }
            });
        }
    }

    static class ReferenceListModule extends SimpleModule {
        public ReferenceListModule() {
            addSerializer(ReferenceList.class, new JsonSerializer<ReferenceList>() {

                @Override
                public void serialize(ReferenceList value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                    jgen.writeStartArray(value.size());
                    for (Object item : value)
                        jgen.writeObject(((Model) item).toReferenceJson());
                    jgen.writeEndArray();
                }
            });
        }
    }
}
