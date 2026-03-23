package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Unprocessable Entity");
        response.put("message", exception.getMessage());
        
        // Return HTTP 422
        return Response.status(422)
                .entity(response)
                .build();
    }
}
