package org.acme.Resources;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.N4JDB.DTOs.MessageNodeDTO;
import org.acme.N4JDB.Nodes.MessageNode;
import org.acme.N4JDB.Repositories.MessageNodeRepository;
import org.acme.Services.MessageNodeService;

import java.util.List;

@Authenticated
@Path("/api/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    MessageNodeService service;

    @Inject
    MessageNodeRepository repository;

    @PUT
    @Path("/update")
    @RolesAllowed({"user", "superuser"})
    public Response update(MessageNodeDTO dto) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            MessageNode entity = repository.findById(dto.getId());
            if (entity == null) {
                throw new NotFoundException("Message not found");
            }
            if (!entity.getSender().getUserId().equals(currentUserId)) {
                throw new ForbiddenException("You can only update your own messages");
            }
            MessageNodeDTO updated = service.update(dto);
            return Response.ok(updated).build();
        } catch (Exception e) {
            String msg = "Unexpected error while updating message: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @PUT
    @Path("/read/{id}")
    @RolesAllowed({"user", "superuser"})
    public Response markAsRead(@PathParam("id") Long id) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            MessageNode entity = repository.findById(id);
            if (entity == null) {
                throw new NotFoundException("Message not found");
            }
            if (!entity.getRecipient().getUserId().equals(currentUserId)) {
                throw new ForbiddenException("You can only mark messages received by yourself as read");
            }
            service.markAsRead(id);
            return Response.ok().build();
        } catch (Exception e) {
            String msg = "Unexpected error while marking message as read: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @PUT
    @Path("/edit/{id}")
    @RolesAllowed({"user", "superuser"})
    public Response editMessage(@PathParam("id") Long id, MessageNodeDTO dto) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            MessageNode entity = repository.findById(id);
            if (entity == null) {
                throw new NotFoundException("Message not found");
            }
            if (!entity.getSender().getUserId().equals(currentUserId)) {
                throw new ForbiddenException("You can only edit your own messages");
            }
            dto.setId(id);
            MessageNodeDTO updated = service.update(dto);
            return Response.ok(updated).build();
        } catch (Exception e) {
            String msg = "Unexpected error while editing message: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @DELETE
    @Path("/delete/{id}")
    @RolesAllowed({"user", "superuser"})
    public Response delete(@PathParam("id") Long id) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            boolean isSuperuser = identity.getRoles().contains("superuser");
            MessageNode entity = repository.findById(id);
            if (entity == null) {
                throw new NotFoundException("Message not found");
            }
            if (!isSuperuser && !entity.getSender().equals(currentUserId)) {
                throw new ForbiddenException("You can only delete your own messages");
            }
            service.delete(id);
            return Response.ok().build();
        } catch (Exception e) {
            String msg = "Unexpected error while deleting message: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    @Path("/between/{user1Id}/{user2Id}")
    @RolesAllowed({"user", "superuser"})
    public Response getMessagesFromTo(
            @PathParam("user1Id") String user1Id,
            @PathParam("user2Id") String user2Id,
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("skip") @DefaultValue("0") int skip
    ) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            if (!(user1Id.equals(currentUserId) || user2Id.equals(currentUserId))) {
                throw new ForbiddenException("You can only retrieve messages where you are one of the participants");
            }

            List<MessageNodeDTO> messages = service.getMessagesBetweenPaginated(
                    user1Id,
                    user2Id,
                    limit,
                    skip
            );

            return Response.ok(messages).build();
        } catch (Exception e) {
            String msg = "Unexpected error while retrieving messages: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @PUT
    @Path("/read-up-to/{lastId}")
    @RolesAllowed({"user", "superuser"})
    public Response markAsReadUpTo(@PathParam("lastId") Long lastId) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            service.markAsReadUpTo(lastId, currentUserId);
            return Response.ok().build();
        } catch (Exception e) {
            String msg = "Unexpected error while marking messages as read up to: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }
}