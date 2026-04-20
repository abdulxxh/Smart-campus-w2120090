package com.smartcampus.config;

import org.glassfish.jersey.server.ResourceConfig;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {
    public SmartCampusApplication() {
        // Explicitly register resources to avoid Maven exec classloader scanning issues
        register(com.smartcampus.resources.DiscoveryResource.class);
        register(com.smartcampus.resources.SensorRoomResource.class);
        register(com.smartcampus.resources.SensorResource.class);
        // SensorReadingResource is a sub-resource mapping, so it doesn't need to be registered here
        
        // Register Exception Mappers
        register(com.smartcampus.exceptions.GlobalExceptionMapper.class);
        register(com.smartcampus.exceptions.LinkedResourceNotFoundExceptionMapper.class);
        register(com.smartcampus.exceptions.RoomNotEmptyExceptionMapper.class);
        register(com.smartcampus.exceptions.SensorUnavailableExceptionMapper.class);
        
        // Register Filters
        register(com.smartcampus.filters.LoggingFilter.class);
    }
}
