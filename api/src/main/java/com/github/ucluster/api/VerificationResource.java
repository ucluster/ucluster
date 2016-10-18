package com.github.ucluster.api;

import com.github.ucluster.verification.VerificationCodeGenerator;
import com.github.ucluster.verification.VerificationRegistry;
import com.github.ucluster.verification.VerificationService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

@Path("verifications")
public class VerificationResource {

    @Inject
    VerificationRegistry registry;

    @Inject
    VerificationCodeGenerator generator;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendVerification(Map<String, Object> request) {
        final String code = generator.generate();
        final Optional<VerificationService> service = registry.find((String) request.get("type"));

        if (service.isPresent()) {
            service.get().send(String.valueOf(request.get("target")), code);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
