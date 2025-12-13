package org.acme.Resources;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.PGDB.DTOs.UserGameListEntryDTO;
import org.acme.Services.UserGameListEntryService;

import java.util.List;
import java.util.UUID;

@Path("/api/usergamelistentries")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserGameListEntryResource {

    @Inject
    UserGameListEntryService userGameListEntryService;

    @Inject
    SecurityIdentity identity;

    @POST
    @Path("/create")
    @RolesAllowed({"user", "superuser"})
    public Response create(UserGameListEntryDTO dto) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            if (!currentUserId.equals(dto.getUserId().toString())) {
                throw new ForbiddenException("You can only add entries to your own list");
            }
            UserGameListEntryDTO created = userGameListEntryService.create(dto);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (Exception e) {
            String msg = "Unexpected error while creating user game list entry: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @PUT
    @Path("/update")
    @RolesAllowed({"user", "superuser"})
    public Response update(UserGameListEntryDTO dto) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            if (!currentUserId.equals(dto.getUserId().toString())) {
                throw new ForbiddenException("You can only update entries in your own list");
            }
            UserGameListEntryDTO updated = userGameListEntryService.update(dto);
            return Response.ok(updated).build();
        } catch (Exception e) {
            String msg = "Unexpected error while updating user game list entry: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    @Path("/getEntriesForUser/{userId}")
    @RolesAllowed({"user", "superuser"})
    public Response getEntriesForUser(@PathParam("userId") UUID userId) {
        try {
            List<UserGameListEntryDTO> list = userGameListEntryService.getEntriesForUser(userId);
            return Response.ok(list).build();
        } catch (Exception e) {
            String msg = "Unexpected error while retrieving user game list entries: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @DELETE
    @Path("/delete")
    @RolesAllowed({"user", "superuser"})
    public Response delete(UserGameListEntryDTO dto) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            if (!currentUserId.equals(dto.getUserId().toString())) {
                throw new ForbiddenException("You can only delete entries from your own list");
            }
            userGameListEntryService.delete(dto.getId());
            return Response.noContent().build();
        } catch (Exception e) {
            String msg = "Unexpected error while deleting user game list entry: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }
}