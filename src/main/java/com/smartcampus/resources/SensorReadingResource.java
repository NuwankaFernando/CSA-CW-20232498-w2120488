package com.smartcampus.resources;

import com.smartcampus.dao.BaseDAO;
import com.smartcampus.dao.MockDatabase;
import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/* - Part 4: Deep Nesting with Sub - Resources (20 Marks)
    - 2. Historical Data Management (10 Marks) */
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    private Map<String, List<SensorReading>> sensorReadingDAO = MockDatabase.READINGS;
    private BaseDAO<Sensor> sensorDAO = new BaseDAO<>(MockDatabase.SENSORS);

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = sensorDAO.getById(sensorId);
        List<SensorReading> idReadings = sensorReadingDAO.get(sensorId);
        return Response.status(Response.Status.OK)
                .entity(Map.of("sensorId", sensor.getId(),
                        "type", sensor.getType(),
                        "status", sensor.getStatus(),
                        "currentValue", sensor.getCurrentValue(),
                        "room", sensor.getRoomId(),
                        "readings", idReadings)
                )
                .build();

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {

        /* - Part 5: Advanced Error Handling, Exception Mapping & Logging (30 Marks)
       - 3. State Constraint (403 Forbidden) (5 Marks) */
        if ("MAINTENANCE".equalsIgnoreCase(sensorDAO.getById(sensorId).getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        if (reading == null) {
            return Response.status(422)
                    .entity(Map.of(
                            "status", 422,
                            "error", "Unprocessable Entity",
                            "message", "Reading body is required."
                    ))
                    .build();
        }

        // Auto-generate ID and timestamp if not provided
        SensorReading newReading = new SensorReading(reading.getValue());
        if (sensorReadingDAO.get(sensorId) == null) {
            sensorReadingDAO.put(sensorId, new ArrayList<>());

        }
        sensorReadingDAO.get(sensorId).add(reading);

        // Side effect: keep parent sensor's currentValue in sync
        sensorDAO.getById(sensorId).setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }

}
