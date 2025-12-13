

package org.acme.Resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.Misc.ArticleUploadForm;
import org.acme.PGDB.DTOs.ArticleDTO;
import org.acme.Services.ArticleService;
import org.acme.Services.ImageService;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.util.List;

@Path("/api/articles")
public class ArticleResource {

    @Inject
    ArticleService articleService;

    @Inject
    ImageService imageService;

    @POST
    @Path("/create")
    @RolesAllowed({"superuser"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@BeanParam ArticleUploadForm form) throws IOException {
        ArticleDTO dto = form.data;
        String imageUrl = imageService.uploadImage(form.file, dto.getFeaturedImageUrl(), "articles");
        dto.setFeaturedImageUrl(imageUrl != null ? imageUrl : imageService.getDefaultCoverUrl());

        ArticleDTO created = articleService.create(dto);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    @PUT
    @Path("/update")
    @RolesAllowed({"superuser"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response update(@BeanParam ArticleUploadForm form) throws IOException {
        try {
            ArticleDTO dto = form.data;
            String imageUrl = imageService.uploadImage(form.file, dto.getFeaturedImageUrl(), "articles");
            if (imageUrl != null) {
                dto.setFeaturedImageUrl(imageUrl);
            }

            ArticleDTO updated = articleService.update(dto);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(iae.getMessage())
                    .build();
        } catch (Exception e) {
            String msg = "Unexpected error while updating article: " + e.getMessage();
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
            List<ArticleDTO> list = articleService.getAll();
            return Response.ok(list).build();
        } catch (Exception e) {
            String msg = "Unexpected error while retrieving articles: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }
}