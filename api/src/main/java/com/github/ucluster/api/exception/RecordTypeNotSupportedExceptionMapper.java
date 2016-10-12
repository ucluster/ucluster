package com.github.ucluster.api.exception;

import com.github.ucluster.core.exception.RecordTypeNotSupportedException;
import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.Map;

@Provider
public class RecordTypeNotSupportedExceptionMapper implements ExceptionMapper<RecordTypeNotSupportedException> {
    @Override
    public Response toResponse(RecordTypeNotSupportedException exception) {
        Map<String, Object> error = ImmutableMap.<String, Object>builder()
                .put("cause", "unsupported.type")
                .put("type", exception.getType())
                .build();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ImmutableMap.<String, Object>builder()
                        .put("errors", Collections.singletonList(error)).build())
                .build();
    }
}
