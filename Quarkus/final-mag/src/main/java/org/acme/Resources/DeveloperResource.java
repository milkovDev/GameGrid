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
import org.acme.PGDB.DTOs.DeveloperDTO;
import org.acme.Services.DeveloperService;
import org.acme.Validation.DuplicateException;
import org.acme.Validation.DuplicateValidator;

import java.util.List;

@Path("/api/developers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeveloperResource {

    @Inject
    DeveloperService developerService;

    @Inject
    DuplicateValidator duplicateValidator;

    @POST
    @Path("/create")
    @RolesAllowed({"superuser"})
    public Response create(DeveloperDTO dto) {
        try {
            duplicateValidator.checkDeveloperName(dto.getName());
            DeveloperDTO created = developerService.create(dto);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (DuplicateException de) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(de.getMessage())
                    .build();
        } catch (Exception e) {
            String msg = "Unexpected error while creating developer: " + e.getMessage();
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
            List<DeveloperDTO> list = developerService.getAll();
            return Response.ok(list).build();
        } catch (Exception e) {
            String msg = "Unexpected error while retrieving developers: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }
}