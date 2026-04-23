package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // If it's a standard JAX-RS framework exception (like 404, 415, 400), don't mask it
        if (exception instanceof jakarta.ws.rs.WebApplicationException) {
            return ((jakarta.ws.rs.WebApplicationException) exception).getResponse();
        }
        
        // Generate a tracking ID so server logs can be matched with client error
        String errorId = UUID.randomUUID().toString();
        
        // Log the actual stack trace server-side with severe level
        LOGGER.log(Level.SEVERE, "Internal Server Error [" + errorId + "]: ", exception);
        
        // Return an obfuscated/safe error message to the client
        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred. Please contact support.");
        response.put("errorId", errorId);
        
        return Response.serverError().entity(response).build();
    }
}
