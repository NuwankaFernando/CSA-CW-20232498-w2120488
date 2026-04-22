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

/* Part 2: Room Management (20 Marks) */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private BaseDAO<Room> roomDAO = new BaseDAO<>(MockDatabase.ROOMS);
    private BaseDAO<Sensor> sensorDAO = new BaseDAO<>(MockDatabase.SENSORS);

    /* 1. Room Resource Implementation (10 Marks) */
    @GET
    public Response getAllRooms() {
        return Response.status(Response.Status.OK)
                .entity(roomDAO.getAll())
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRoom(Room room) {

        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(422)
                    .entity(Map.of(
                            "status", 422,
                            "error", "Unprocessable Entity",
                            "message", "Room ID is required."
                    ))
                    .build();
        }

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
        roomDAO.add(room);
        URI location = UriBuilder.fromPath("/api/v1/rooms/{id}").build(room.getId());
        return Response.status(Response.Status.CREATED).location(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {

        List<Room> rooms = roomDAO.getAll();
        for (Room r : rooms) {
            if (r.getId().equals(roomId)) {
                return Response.status(Response.Status.OK)
                        .entity(roomDAO.getById(roomId))
                        .build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(
                        Map.of(
                                "status", 404,
                                "error", "Not Found",
                                "message", "Room '" + roomId + "' not found."
                        ))
                .build();
    }

    /* 2. Room Deletion & Safety Logic (10 Marks) */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = roomDAO.getById(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(
                            Map.of(
                                    "status", 404,
                                    "error", "Not Found",
                                    "message", "Room '" + roomId + "' not found."
                            ))
                    .build();
        }

        List<Sensor> sensors = sensorDAO.getAll();
        List<String> roomSensors = roomDAO.getById(roomId).getSensorIds();
        for (String sr : roomSensors) {
            for (Sensor s : sensors) {
                if (sr.equals(s.getId()) && s.getStatus().equals("ACTIVE")) {

                    /* - Part 5: Advanced Error Handling, Exception Mapping & Logging (30 Marks)
                    - 1. Resource Conflict (409) (5 Marks) */
                    throw new RoomNotEmptyException(roomId);
                }
            }
        }

        roomDAO.delete(room);
        return Response.status(Response.Status.OK)
                .entity(Map.of(
                        "status", 200,
                        "method", "OK",
                        "message", "Room with id " + roomId + " deleted succesfully"
                ))
                .build();
    }

}
