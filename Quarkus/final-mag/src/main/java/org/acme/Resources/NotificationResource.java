package org.acme.Resources;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.N4JDB.DTOs.NotificationNodeDTO;
import org.acme.N4JDB.Nodes.NotificationNode;
import org.acme.N4JDB.Repositories.NotificationNodeRepository;
import org.acme.Services.NotificationNodeService;

import java.util.List;

@Authenticated
@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    NotificationNodeService service;

    @Inject
    NotificationNodeRepository repository;

    @PUT
    @Path("/read/{id}")
    @RolesAllowed({"user", "superuser"})
    public Response markAsRead(@PathParam("id") Long id) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            NotificationNode entity = repository.findById(id);
            if (entity == null) {
                throw new NotFoundException("Notification not found");
            }
            if (!entity.getTarget().getUserId().equals(currentUserId)) {
                throw new ForbiddenException("You can only mark your own notifications as read");
            }
            service.markAsRead(id);
            return Response.ok().build();
        } catch (Exception e) {
            String msg = "Unexpected error while marking notification as read: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @DELETE
    @Path("delete/{id}")
    @RolesAllowed({"user", "superuser"})
    public Response delete(@PathParam("id") Long id) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            NotificationNode entity = repository.findById(id);
            if (entity == null) {
                throw new NotFoundException("Notification not found");
            }
            if (!entity.getTarget().getUserId().equals(currentUserId)) {
                throw new ForbiddenException("You can only delete your own notifications");
            }
            service.delete(id);
            return Response.ok().build();
        } catch (Exception e) {
            String msg = "Unexpected error while deleting notification: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    @Path("/getForUser/{targetId}")
    @RolesAllowed({"user", "superuser"})
    public Response getNotificationsForUser(@PathParam("targetId") String targetId) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            if (!targetId.equals(currentUserId)) {
                throw new ForbiddenException("You can only retrieve your own notifications");
            }
            List<NotificationNodeDTO> notifications = service.getNotificationsForUser(targetId);

            return Response.ok(notifications).build();
        } catch (Exception e) {
            String msg = "Unexpected error while retrieving notifications: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }
}