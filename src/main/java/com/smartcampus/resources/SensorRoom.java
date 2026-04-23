package com.smartcampus.resources;

import com.smartcampus.dao.BaseDAO;
import com.smartcampus.dao.MockDatabase;
import com.smartcampus.exceptions.RoomNotEmptyException;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Nuwanka Fernando - Part 2: Question 1 and 2
 *
 */
// Resource class for managing rooms.
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class SensorRoom {

    // Data access objects backed by mock in-memory storage
    private BaseDAO<Room> roomDAO = new BaseDAO<>(MockDatabase.ROOMS);
    private BaseDAO<Sensor> sensorDAO = new BaseDAO<>(MockDatabase.SENSORS);

    // Retrieves all rooms.
    @GET
    public Response getAllRooms() {
        return Response.status(Response.Status.OK)
                .entity(roomDAO.getAll())
                .build();
    }

    // Creates a new room.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRoom(Room room) {

        // Validate required room ID
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(422)
                    .entity(Map.of(
                            "status", 422,
                            "error", "Unprocessable Entity",
                            "message", "Room ID is required."
                    ))
                    .build();
        }

        // Check for duplicate room ID
        List<Room> rooms = roomDAO.getAll();
        for (Room r : rooms) {
            if (r.getId().equals(room.getId())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of(
                                "status", 409,
                                "error", "Conflict",
                                "message", "A room with ID '" + room.getId() + "' already exists."
                        ))
                        .build();
            }
        }

        // Persist room
        roomDAO.add(room);

        // Build Location header for the created resource
        URI location = UriBuilder.fromPath("/api/v1/rooms/{id}")
                .build(room.getId());

        return Response.status(Response.Status.CREATED)
                .location(location)
                .entity(room)
                .build();
    }

    // Retrieves a specific room by ID.
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {

        List<Room> rooms = roomDAO.getAll();

        // Search for matching room
        for (Room r : rooms) {
            if (r.getId().equals(roomId)) {
                return Response.status(Response.Status.OK)
                        .entity(roomDAO.getById(roomId))
                        .build();
            }
        }

        // Return 404 if room does not exist
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                        "status", 404,
                        "error", "Not Found",
                        "message", "Room '" + roomId + "' not found."
                ))
                .build();
    }

    // Deletes a room if no active sensors are linked to it.
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        Room room = roomDAO.getById(roomId);

        // Validate room existence
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                            "status", 404,
                            "error", "Not Found",
                            "message", "Room '" + roomId + "' not found."
                    ))
                    .build();
        }

        // Retrieve all sensors and those linked to this room
        List<Sensor> sensors = sensorDAO.getAll();
        List<String> roomSensors = room.getSensorIds();

        // Prevent deletion if any linked sensor is ACTIVE
        for (String sr : roomSensors) {
            for (Sensor s : sensors) {
                if (sr.equals(s.getId()) && "ACTIVE".equals(s.getStatus())) {
                    throw new RoomNotEmptyException("Room '" + roomId + "' cannot be deleted: it still has active sensors assigned.");
                }
            }
        }

        // Delete room from storage
        roomDAO.delete(room);

        // Return success response
        return Response.status(Response.Status.OK)
                .entity(Map.of(
                        "status", 200,
                        "method", "OK",
                        "message", "Room with id " + roomId + " deleted successfully"
                ))
                .build();
    }
}
