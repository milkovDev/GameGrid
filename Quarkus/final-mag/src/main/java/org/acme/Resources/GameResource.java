
package org.acme.Resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.Misc.GameUploadForm;
import org.acme.PGDB.DTOs.GameDTO;
import org.acme.Services.GameService;
import org.acme.Services.ImageService;
import org.acme.Validation.DuplicateException;
import org.acme.Validation.DuplicateValidator;
import jakarta.ws.rs.BeanParam;

import java.io.IOException;
import java.util.List;

@Path("/api/games")
public class GameResource {

    @Inject
    GameService gameService;

    @Inject
    DuplicateValidator duplicateValidator;

    @Inject
    ImageService imageService;

    @POST
    @Path("/create")
    @RolesAllowed({"superuser"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@BeanParam GameUploadForm form) {
        try {
            GameDTO dto = form.data;
            String coverUrl = imageService.uploadImage(form.file, dto.getCoverUrl(), "games");
            dto.setCoverUrl(coverUrl != null ? coverUrl : imageService.getDefaultCoverUrl());

            duplicateValidator.checkGameTitle(dto.getTitle());
            GameDTO created = gameService.create(dto);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (DuplicateException de) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(de.getMessage())
                    .build();
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(iae.getMessage())
                    .build();
        } catch (Exception e) {
            String msg = "Unexpected error while creating game: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @PUT
    @Path("/update")
    @RolesAllowed({"superuser"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response update(@BeanParam GameUploadForm form) throws IOException {
        try {
            GameDTO dto = form.data;
            String coverUrl = imageService.uploadImage(form.file, dto.getCoverUrl(), "games");
            if (coverUrl != null) {
                dto.setCoverUrl(coverUrl);
            }

            duplicateValidator.checkGameTitle(dto.getTitle());
            GameDTO updated = gameService.update(dto);
            return Response.ok(updated).build();
        } catch (DuplicateException de) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(de.getMessage())
                    .build();
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(iae.getMessage())
                    .build();
        } catch (Exception e) {
            String msg = "Unexpected error while updating game: " + e.getMessage();
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
            List<GameDTO> list = gameService.getAll();
            return Response.ok(list).build();
        } catch (Exception e) {
            String msg = "Unexpected error while retrieving games: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

}