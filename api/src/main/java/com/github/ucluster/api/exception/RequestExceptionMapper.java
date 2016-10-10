package com.github.ucluster.api.exception;

import com.github.ucluster.core.exception.RequestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RequestExceptionMapper implements ExceptionMapper<RequestException> {
    @Override
    public Response toResponse(RequestException exception) {
        return Response.status(400).build();
    }
}
