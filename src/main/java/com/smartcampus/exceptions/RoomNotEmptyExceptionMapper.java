package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Conflict");
        response.put("message", exception.getMessage());
        
        return Response.status(Response.Status.CONFLICT)
                .entity(response)
                .build();
    }
}
