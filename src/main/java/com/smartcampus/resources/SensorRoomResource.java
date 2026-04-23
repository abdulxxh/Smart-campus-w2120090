package com.smartcampus.resources;

import com.smartcampus.exceptions.RoomNotEmptyException;
import com.smartcampus.models.Room;
import com.smartcampus.repository.InMemoryDataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private Map<String, Room> rooms = InMemoryDataStore.getRooms();

    @GET
    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        // Simple generation of ID if not provided
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            room.setId("RM-" + System.currentTimeMillis());
        }
        
        rooms.put(room.getId(), room);
        
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Room not found\"}").build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            // Can be considered idempotent, returning 404 or success. We'll return 404 for clarity.
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Room not found\"}").build();
        }
        
        // Business Logic Constraint: cannot be deleted if it has active sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " cannot be deleted because it still has active sensors assigned to it.");
        }
        
        rooms.remove(roomId);
        return Response.noContent().build();
    }
}
