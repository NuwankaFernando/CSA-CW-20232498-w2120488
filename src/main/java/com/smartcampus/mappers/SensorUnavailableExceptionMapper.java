package com.smartcampus.mappers;

import com.smartcampus.exceptions.SensorUnavailableException;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Nuwanka Fernando - Part 5: Question 3
 *
 */
// Exception mapper for handling sensor state constraints.
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    // Converts SensorUnavailableException into HTTP 403 response.
    @Override
    public Response toResponse(SensorUnavailableException ex) {

        // Return structured forbidden error response
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "status", 403,
                        "error", "Forbidden",
                        "message", ex.getMessage()
                ))
                .build();
    }
}
