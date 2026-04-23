package com.smartcampus.resources;

import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import com.smartcampus.repository.InMemoryDataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final Map<String, Sensor> sensors = InMemoryDataStore.getSensors();
    private final Map<String, List<SensorReading>> sensorReadingsStore = InMemoryDataStore.getSensorReadings();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        if (!sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Sensor not found\"}").build();
        }
        
        List<SensorReading> readings = sensorReadingsStore.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Sensor not found\"}").build();
        }
        
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) || "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is currently " + sensor.getStatus() + " and cannot accept new readings.");
        }
        
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        
        // Save the reading
        List<SensorReading> readings = sensorReadingsStore.computeIfAbsent(sensorId, k -> new ArrayList<>());
        readings.add(reading);
        
        // Side Effect: update parent sensor
        sensor.setCurrentValue(reading.getValue());
        
        URI location = uriInfo.getAbsolutePathBuilder().path(reading.getId()).build();
        return Response.created(location).entity(reading).build();
    }
}
