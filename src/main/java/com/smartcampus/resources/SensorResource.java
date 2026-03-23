package com.smartcampus.resources;

import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.repository.InMemoryDataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private Map<String, Sensor> sensors = InMemoryDataStore.getSensors();
    private Map<String, Room> rooms = InMemoryDataStore.getRooms();

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> allSensors = sensors.values();
        
        if (type != null && !type.trim().isEmpty()) {
            Collection<Sensor> filtered = allSensors.stream()
                .filter(s -> type.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());
            return Response.ok(filtered).build();
        }
        
        return Response.ok(allSensors).build();
    }

    @POST
    public Response registerSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Validation: room must exist
        if (sensor.getRoomId() == null || !rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room with ID " + sensor.getRoomId() + " does not exist.");
        }
        
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            sensor.setId("SENS-" + System.currentTimeMillis());
        }
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE"); // Default status
        }
        
        sensors.put(sensor.getId(), sensor);
        
        // Add sensor to the room's list of sensors
        Room room = rooms.get(sensor.getRoomId());
        if (room.getSensorIds() != null && !room.getSensorIds().contains(sensor.getId())) {
             room.getSensorIds().add(sensor.getId());
        }
        
        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Sensor not found\"}").build();
        }
        return Response.ok(sensor).build();
    }

    // Part 4: Sub-Resource Locator
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsSubResource(@PathParam("sensorId") String sensorId) {
        // Return a sub-resource instance mapped to the specific sensor context
        return new SensorReadingResource(sensorId);
    }
}
