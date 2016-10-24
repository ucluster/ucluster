package com.github.ucluster.api;

import com.github.ucluster.confirmation.ConfirmationCodeGenerator;
import com.github.ucluster.confirmation.ConfirmationRegistry;
import com.github.ucluster.confirmation.ConfirmationService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

@Path("confirmations")
public class ConfirmationResource {

    @Inject
    ConfirmationRegistry registry;

    @Inject
    ConfirmationCodeGenerator generator;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendConfirmation(Map<String, Object> request) {
        final String code = generator.generate();
        final Optional<ConfirmationService> service = registry.find((String) request.get("type"));

        if (service.isPresent()) {
            service.get().send(String.valueOf(request.get("target")), code);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
