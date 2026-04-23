package com.smartcampus.resources;

import com.smartcampus.dao.BaseDAO;
import com.smartcampus.dao.MockDatabase;
import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Nuwanka Fernando - Part 4: Question 2
 *
 */
// Handles retrieval and creation of historical readings for a specific sensor (nested resource).
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    // In-memory data sources
    private Map<String, List<SensorReading>> sensorReadingDAO = MockDatabase.READINGS;
    private BaseDAO<Sensor> sensorDAO = new BaseDAO<>(MockDatabase.SENSORS);

    // Constructor initializes resource with a specific sensor ID.
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // Retrieves all readings associated with the given sensor.
    @GET
    public Response getReadings() {

        Sensor sensor = sensorDAO.getById(sensorId);
        List<SensorReading> idReadings = sensorReadingDAO.get(sensorId);

        // Construct structured response with sensor metadata + readings
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sensorId", sensor.getId());
        response.put("type", sensor.getType());
        response.put("status", sensor.getStatus());
        response.put("currentValue", sensor.getCurrentValue());
        response.put("roomId", sensor.getRoomId());
        response.put("readings", idReadings);

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    // Adds a new reading to the specified sensor.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)

    public Response addReading(SensorReading reading) {

        // Reject updates if sensor is under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensorDAO.getById(sensorId).getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensorId + "' is under MAINTENANCE.");
        }

        // Validate request body
        if (reading == null) {
            return Response.status(422)
                    .entity(Map.of(
                            "status", 422,
                            "error", "Unprocessable Entity",
                            "message", "Reading body is required."
                    ))
                    .build();
        }

        // Create a new reading instance (ensures ID/timestamp handling internally)
        SensorReading newReading = new SensorReading(reading.getValue());

        // Initialize list if no readings exist for this sensor
        if (sensorReadingDAO.get(sensorId) == null) {
            sensorReadingDAO.put(sensorId, new ArrayList<>());
        }

        // Persist reading in mock database
        sensorReadingDAO.get(sensorId).add(reading);

        // Keep sensor's current value synchronized with latest reading
        sensorDAO.getById(sensorId).setCurrentValue(reading.getValue());

        // Return 201 Created with newly created reading
        return Response.status(Response.Status.CREATED)
                .entity(newReading)
                .build();
    }
}
