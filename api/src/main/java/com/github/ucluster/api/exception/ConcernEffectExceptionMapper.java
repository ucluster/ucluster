package com.github.ucluster.api.exception;

import com.github.ucluster.core.exception.ConcernEffectException;
import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class ConcernEffectExceptionMapper implements ExceptionMapper<ConcernEffectException> {
    @Override
    public Response toResponse(ConcernEffectException exception) {
        final List<Map<String, String>> errors = exception.getEffectResult().errors().stream()
                .map(e -> ImmutableMap.<String, String>builder()
                        .put("property", e.getPropertyPath())
                        .put("cause", e.getType()).build()
                ).collect(Collectors.toList());

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(
                        ImmutableMap.<String, Object>builder()
                                .put("errors", errors).build()
                ).build();
    }
}
