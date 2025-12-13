package org.acme.Resources;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.File;

@Path("/api/images")
public class ImageResource {

    private static final String BASE_UPLOAD_DIR = "Uploads/";
    private static final String GAMES_UPLOAD_DIR = BASE_UPLOAD_DIR + "games/";
    private static final String ARTICLES_UPLOAD_DIR = BASE_UPLOAD_DIR + "articles/";
    private static final String AVATARS_UPLOAD_DIR = BASE_UPLOAD_DIR + "avatars/";

    @GET
    @Path("/games/{filename}")
    @Produces("image/*")
    @PermitAll
    public Response getGameImage(@PathParam("filename") String filename) {
        File file = new File(GAMES_UPLOAD_DIR + filename);
        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(file).header("Content-Type", getContentType(filename)).build();
    }

    @GET
    @Path("/articles/{filename}")
    @Produces("image/*")
    @PermitAll
    public Response getArticleImage(@PathParam("filename") String filename) {
        File file = new File(ARTICLES_UPLOAD_DIR + filename);
        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(file).header("Content-Type", getContentType(filename)).build();
    }

    @GET
    @Path("/avatars/{filename}")
    @Produces("image/*")
    @PermitAll
    public Response getAvatarImage(@PathParam("filename") String filename) {
        File file = new File(AVATARS_UPLOAD_DIR + filename);
        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(file).header("Content-Type", getContentType(filename)).build();
    }

    @GET
    @Path("/{filename}")
    @Produces("image/*")
    @PermitAll
    public Response getDefaultImage(@PathParam("filename") String filename) {
        File file = new File(BASE_UPLOAD_DIR + filename);
        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(file).header("Content-Type", getContentType(filename)).build();
    }

    private String getContentType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}