package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Forbidden");
        response.put("message", exception.getMessage());
        
        // Return HTTP 403
        return Response.status(Response.Status.FORBIDDEN)
                .entity(response)
                .build();
    }
}
