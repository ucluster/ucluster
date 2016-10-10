package com.github.ucluster.mongo.api.json;

import com.github.ucluster.mongo.Model;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Provider
public class ListWriter implements MessageBodyWriter<List<? extends Model>> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return List.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(List<? extends Model> records, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(List<? extends Model> records, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        Function<Model, Map<String, Object>> toJson =
                records instanceof ReferenceList ? Model::toReferenceJson : Model::toJson;

        try (OutputStreamWriter writer = new OutputStreamWriter(entityStream, "UTF-8")) {
            IOUtils.write(Json.toJson(records.stream().map(toJson).collect(toList())), writer);
        }
    }
}
