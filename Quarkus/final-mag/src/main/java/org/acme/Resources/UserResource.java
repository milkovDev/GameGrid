package org.acme.Resources;

import io.quarkus.oidc.UserInfo;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.Misc.AvatarUploadForm;
import org.acme.N4JDB.DTOs.UserNodeDTO;
import org.acme.N4JDB.Mappers.UserNodeMapper;
import org.acme.N4JDB.Nodes.UserNode;
import org.acme.PGDB.DTOs.CombinedUserDTO;
import org.acme.PGDB.DTOs.DeveloperDTO;
import org.acme.PGDB.DTOs.UserDTO;
import org.acme.PGDB.Entities.User;
import org.acme.PGDB.Mappers.UserMapper;
import org.acme.Services.ImageService;
import org.acme.Services.UserNodeService;
import org.acme.Services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Authenticated
@Path("/api/users")
public class UserResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    UserService userService;

    @Inject
    UserNodeService userNodeService;

    @Inject
    UserMapper userMapper;

    @Inject
    UserNodeMapper userNodeMapper;

    @Inject
    ImageService imageService;

    @GET
    @Path("/me")
    @Authenticated
    public Response getMyInfo() {
        try {
            String idString = identity.getPrincipal().getName();
            UUID uuid = UUID.fromString(idString);

            // Check if records exist, create them if they don't
            User user = userService.getById(uuid);
            if (user == null) {
                // Get display name from UserInfo if available
                UserInfo userInfo = identity.getAttribute("userinfo");
                String displayName = userInfo != null ? userInfo.getString("displayName") : null;

                userService.createUser(uuid, displayName);
                user = userService.getById(uuid);
            }

            UserNode userNode = userNodeService.getById(idString);
            if (userNode == null) {
                // Get display name from UserInfo if available
                UserInfo userInfo = identity.getAttribute("userinfo");
                String displayName = userInfo != null ? userInfo.getString("displayName") : null;

                userNodeService.createUserNode(idString, displayName);
                userNode = userNodeService.getById(idString);
            }

            if (user == null) {
                throw new NotFoundException("Failed to create or find user in PostgreSQL");
            }
            if (userNode == null) {
                throw new NotFoundException("Failed to create or find user in Neo4j");
            }

            UserDTO userDTO = userMapper.toDTO(user);
            UserNodeDTO userNodeDTO = userNodeMapper.toDTO(userNode);

            return Response.ok(new CombinedUserDTO(userDTO, userNodeDTO)).build();
        } catch (Exception e) {
            String msg = "Unexpected error while getting my info: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    @Path("/{userId}")
    public Response getUser(@PathParam("userId") String userId) {
        try {
            UUID uuid = UUID.fromString(userId);

            User user = userService.getById(uuid);
            if (user == null) {
                throw new NotFoundException("User not found in PostgreSQL");
            }

            UserDTO userDTO = userMapper.toDTO(user);

            return Response.ok(userDTO).build();
        } catch (Exception e) {
            String msg = "Unexpected error while getting user: " + e.getMessage();
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
            List<UserDTO> list = userService.getAll();
            return Response.ok(list).build();
        } catch (Exception e) {
            String msg = "Unexpected error while retrieving users: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    @Path("followers/{userId}")
    public Response getUserFollowers(@PathParam("userId") String userId) {
        try {
            UserNode userNode = userNodeService.getById(userId);
            if (userNode == null) {
                throw new NotFoundException("User not found in Neo4j");
            }

            Set<UserNode> followers = userNode.getFollowers();
            List<UserDTO> dtos = new ArrayList<>();
            for (UserNode followerNode : followers) {
                UUID uuid = UUID.fromString(followerNode.getUserId());
                User user = userService.getById(uuid);
                if (user != null) {
                    UserDTO dto = userMapper.toDTO(user);
                    dto.setUserGameListEntries(null);
                    dtos.add(dto);
                }
            }

            return Response.ok(dtos).build();
        } catch (Exception e) {
            String msg = "Unexpected error while getting user followers: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    @Path("following/{userId}")
    public Response getUserFollowing(@PathParam("userId") String userId) {
        try {
            UserNode userNode = userNodeService.getById(userId);
            if (userNode == null) {
                throw new NotFoundException("User not found in Neo4j");
            }

            Set<UserNode> following = userNode.getFollowing();
            List<UserDTO> dtos = new ArrayList<>();
            for (UserNode followedNode : following) {
                UUID uuid = UUID.fromString(followedNode.getUserId());
                User user = userService.getById(uuid);
                if (user != null) {
                    UserDTO dto = userMapper.toDTO(user);
                    dto.setUserGameListEntries(null);
                    dtos.add(dto);
                }
            }

            return Response.ok(dtos).build();
        } catch (Exception e) {
            String msg = "Unexpected error while getting user following: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @PUT
    @Path("/update")
    @RolesAllowed({"user", "superuser"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response update(@BeanParam AvatarUploadForm form) {
        try {
            String currentUserId = identity.getPrincipal().getName();
            UserDTO userDTO = form.getData();
            if (!currentUserId.equals(userDTO.getId().toString())) {
                throw new ForbiddenException("You can only update your own profile");
            }

            String avatarUrl = imageService.uploadImage(form.file, userDTO.getAvatarUrl(), "avatars");
            if (avatarUrl != null) {
                userDTO.setAvatarUrl(avatarUrl);
            }

            UserDTO updatedUserDTO = userService.updateUser(userDTO);
            return Response.ok(updatedUserDTO).build();
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(iae.getMessage())
                    .build();
        }  catch (Exception e) {
            String msg = "Unexpected error while updating user: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @POST
    @Path("/follow/{followedId}")
    @RolesAllowed({"user", "superuser"})
    public Response followUser(@PathParam("followedId") String followedId) {
        try {
            String followerId = identity.getPrincipal().getName();
            boolean success = userNodeService.follow(followerId, followedId);
            if (success) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            String msg = "Unexpected error while following user: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }

    @DELETE
    @Path("/unfollow/{followedId}")
    @RolesAllowed({"user", "superuser"})
    public Response unfollowUser(@PathParam("followedId") String followedId) {
        try {
            String followerId = identity.getPrincipal().getName();
            boolean success = userNodeService.unfollow(followerId, followedId);
            if (success) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            String msg = "Unexpected error while unfollowing user: " + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg)
                    .build();
        }
    }
}