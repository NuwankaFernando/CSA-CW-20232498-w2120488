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

/* Part 3: Sensor Operations & Linking (20 Marks) */
 /* Part 4: Deep Nesting with Sub - Resources (20 Marks) */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private BaseDAO<Sensor> sensorDAO = new BaseDAO<>(MockDatabase.SENSORS);
    private BaseDAO<Room> roomDAO = new BaseDAO<>(MockDatabase.ROOMS);

    /* - Part 3: Sensor Operations & Linking (20 Marks)
    - 2. Filtered Retrieval & Search (10 Marks)*/
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {

        List<Sensor> sensors = sensorDAO.getAll();

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

        return Response.status(Response.Status.OK)
                .entity(sensors)
                .build();
    }

    /* - Part 3: Sensor Operations & Linking (20 Marks)
    - 1.Sensor Resource & Integrity (10 Marks) */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addSensor(Sensor sensor) {

        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(422)
                    .entity(Map.of(
                            "status", 422,
                            "error", "Unprocessable Entity",
                            "message", "Sensor ID is required."
                    ))
                    .build();
        }

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

        List<Room> rooms = roomDAO.getAll();
        List<String> roomIds = new ArrayList<>();
        for (Room r : rooms) {
            roomIds.add(r.getId());
        }

        /* - Part 5: Advanced Error Handling, Exception Mapping & Logging (30 Marks)
        - 2. Dependency Validation (422 Unprocessable Entity) (10 Marks) */
        if (!roomIds.contains(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }

        sensorDAO.add(sensor);
        roomDAO.getById(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();

    }

    @GET
    @Path("/{sensorId}")
    public Response getRoomById(@PathParam("sensorId") String sensorId) {

        List<Sensor> sensors = sensorDAO.getAll();
        for (Sensor s : sensors) {
            if (s.getId().equals(sensorId)) {
                return Response.status(Response.Status.OK)
                        .entity(sensorDAO.getById(sensorId))
                        .build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(
                        Map.of(
                                "status", 404,
                                "error", "Not Found",
                                "message", "Sensor '" + sensorId + "' not found."
                        ))
                .build();
    }

    /* - Part 4: Deep Nesting with Sub - Resources (20 Marks)
    - 1. The Sub-Resource Locator Pattern (10 Marks) */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorDAO.getById(sensorId);
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
        return new SensorReadingResource(sensorId);
    }

}
