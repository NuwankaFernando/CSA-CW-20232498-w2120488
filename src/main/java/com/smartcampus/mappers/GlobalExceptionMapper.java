package com.smartcampus.mappers;

import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Nuwanka Fernando - Part 5: Question 4
 *
 */
// Global exception mapper for handling unanticipated errors.
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    // Converts any uncaught exception into a structured HTTP response.
    @Override
    public Response toResponse(Throwable exception) {

        // Return a generic error response without exposing internal details
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "An unexpected error occurred. Please try again later."
                ))
                .build();
    }
}
