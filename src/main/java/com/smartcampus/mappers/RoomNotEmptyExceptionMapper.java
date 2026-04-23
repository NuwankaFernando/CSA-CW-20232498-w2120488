package com.smartcampus.mappers;

import com.smartcampus.exceptions.RoomNotEmptyException;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Nuwanka Fernando - Part 5: Question 1
 *
 */
// Exception mapper for handling room deletion conflicts.
@Provider
public class RoomNotEmptyExceptionMapper
        implements ExceptionMapper<RoomNotEmptyException> {

    // Converts RoomNotEmptyException into HTTP 409 response.
    @Override
    public Response toResponse(RoomNotEmptyException ex) {

        // Return structured conflict error response
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "status", 409,
                        "error", "Conflict",
                        "message", ex.getMessage()
                ))
                .build();
    }
}
