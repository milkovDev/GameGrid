package org.acme.Resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.PGDB.DTOs.PublisherDTO;
import org.acme.Services.PublisherService;
import org.acme.Validation.DuplicateException;
import org.acme.Validation.DuplicateValidator;

import java.util.List;

@Path("/api/publishers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublisherResource {

    @Inject
    PublisherService publisherService;

    @Inject
    DuplicateValidator duplicateValidator;

    @POST
    @Path("/create")
    @RolesAllowed({"superuser"})
    public Response create(PublisherDTO dto) {
        try {
            duplicateValidator.checkPublisherName(dto.getName());
            PublisherDTO created = publisherService.create(dto);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (DuplicateException de) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(de.getMessage())
                    .build();
        } catch (Exception e) {
            String msg = "Unexpected error while creating publisher: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    @Path("/getAll")
    @RolesAllowed({"user", "superuser"})
    public Response getAll() {
        try {
            List<PublisherDTO> list = publisherService.getAll();
            return Response.ok(list).build();
        } catch (Exception e) {
            String msg = "Unexpected error while retrieving publishers: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }
}