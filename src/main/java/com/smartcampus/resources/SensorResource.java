package com.smartcampus.resources;

import com.smartcampus.dao.BaseDAO;
import com.smartcampus.dao.MockDatabase;
import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Nuwanka Fernando - Part 4: Question 1 and 2
 *
 */
// Resource class for managing sensors.
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    // Data access objects backed by mock in-memory storage
    private BaseDAO<Sensor> sensorDAO = new BaseDAO<>(MockDatabase.SENSORS);
    private BaseDAO<Room> roomDAO = new BaseDAO<>(MockDatabase.ROOMS);

    // Retrieves all sensors, optionally filtered by type.
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {

        List<Sensor> sensors = sensorDAO.getAll();

        // Apply filtering if query parameter is provided
        if (type != null && !type.isBlank()) {
            List<Sensor> typeSensors = new ArrayList<>();

            for (Sensor s : sensors) {
                if (s.getType().equals(type)) {
                    typeSensors.add(s);
                }
            }

            return Response.status(Response.Status.OK)
                    .entity(typeSensors)
                    .build();
        }

        // Return all sensors if no filter is applied
        return Response.status(Response.Status.OK)
                .entity(sensors)
                .build();
    }

    // Creates a new sensor and links it to an existing room.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addSensor(Sensor sensor) {

        // Validate required sensor ID
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(422)
                    .entity(Map.of(
                            "status", 422,
                            "error", "Unprocessable Entity",
                            "message", "Sensor ID is required."
                    ))
                    .build();
        }

        // Check for duplicate sensor ID
        List<Sensor> sensors = sensorDAO.getAll();
        for (Sensor s : sensors) {
            if (s.getId().equals(sensor.getId())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of(
                                "status", 409,
                                "error", "Conflict",
                                "message", "A sensor with ID '" + sensor.getId() + "' already exists."
                        ))
                        .build();
            }
        }

        // Collect valid room IDs for validation
        List<Room> rooms = roomDAO.getAll();
        List<String> roomIds = new ArrayList<>();
        for (Room r : rooms) {
            roomIds.add(r.getId());
        }

        // Ensure sensor is linked to an existing room
        if (!roomIds.contains(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("No room with ID '" + sensor.getRoomId() + "' does not exist.");
        }

        // Persist sensor and update room linkage
        sensorDAO.add(sensor);
        roomDAO.getById(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }

    // Retrieves a specific sensor by its ID.
    @GET
    @Path("/{sensorId}")
    public Response getRoomById(@PathParam("sensorId") String sensorId) {

        List<Sensor> sensors = sensorDAO.getAll();

        // Search for matching sensor
        for (Sensor s : sensors) {
            if (s.getId().equals(sensorId)) {
                return Response.status(Response.Status.OK)
                        .entity(sensorDAO.getById(sensorId))
                        .build();
            }
        }

        // Return 404 if sensor does not exist
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                        "status", 404,
                        "error", "Not Found",
                        "message", "Sensor '" + sensorId + "' not found."
                ))
                .build();
    }

    // Delegates requests to SensorReadingResource if sensor exists
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {

        Sensor sensor = sensorDAO.getById(sensorId);

        // Validate sensor existence before delegating
        if (sensor == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of(
                                    "status", 404,
                                    "error", "Not Found",
                                    "message", "Sensor '" + sensorId + "' not found."
                            ))
                            .type(MediaType.APPLICATION_JSON)
                            .build()
            );
        }

        // Return sub-resource for handling nested routes
        return new SensorReadingResource(sensorId);
    }
}
