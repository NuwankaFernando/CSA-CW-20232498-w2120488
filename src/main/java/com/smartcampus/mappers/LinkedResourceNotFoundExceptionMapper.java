package com.smartcampus.mappers;

import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Nuwanka Fernando - Part 5: Question 2
 *
 */
// Exception mapper for handling invalid linked resource references.
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    // Converts LinkedResourceNotFoundException into HTTP 422 response.
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {

        // Return structured validation error response
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "status", 422,
                        "error", "Unprocessable Entity",
                        "message", ex.getMessage()
                ))
                .build();
    }
}
