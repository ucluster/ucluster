package com.github.ucluster.mongo.api.json;


import com.github.ucluster.core.util.Page;
import com.github.ucluster.mongo.Model;

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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Provider
public class PageToJson implements MessageBodyWriter<Page<? extends Model>> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Page.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Page<? extends Model> page, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(Page<? extends Model> page, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        Function<Model, Map<String, Object>> toJson =
                page.getEntries() instanceof ReferenceList ? Model::toReferenceJson : Model::toJson;

        Map<String, Object> pageJson = new HashMap<>();
        pageJson.put("page", page.getPage());
        pageJson.put("per_page", page.getPerPage());
        pageJson.put("total_count", page.getTotalEntriesCount());
        pageJson.put("total_page", page.getTotalPagesCount());
        pageJson.put("entries", page.getEntries().stream().map(toJson).collect(toList()));
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(entityStream, "UTF-8")) {
            outputStreamWriter.write(Json.toJson(pageJson));
        }
    }
}
